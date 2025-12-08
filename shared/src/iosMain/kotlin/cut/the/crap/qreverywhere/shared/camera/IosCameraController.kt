package cut.the.crap.qreverywhere.shared.camera

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.UIKit.*
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_async

/**
 * iOS Camera View Controller for QR code scanning
 * Uses AVCaptureSession with AVCaptureMetadataOutput for real-time QR detection
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosCameraViewController(
    private val onQrCodeDetected: (String) -> Unit,
    private val onError: (String) -> Unit
) : UIViewController(nibName = null, bundle = null), AVCaptureMetadataOutputObjectsDelegateProtocol {

    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var isSessionRunning = false
    private var hasDetectedCode = false

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.blackColor
        setupCamera()
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        startSession()
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        stopSession()
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        previewLayer?.frame = view.bounds
    }

    private fun setupCamera() {
        val session = AVCaptureSession()
        session.sessionPreset = AVCaptureSessionPresetHigh

        // Get the back camera
        val videoDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (videoDevice == null) {
            onError("No camera available")
            return
        }

        // Create video input
        val videoInput = try {
            AVCaptureDeviceInput.deviceInputWithDevice(videoDevice, error = null)
        } catch (e: Exception) {
            onError("Failed to create camera input: ${e.message}")
            return
        }

        if (videoInput == null) {
            onError("Failed to create camera input")
            return
        }

        if (session.canAddInput(videoInput)) {
            session.addInput(videoInput)
        } else {
            onError("Cannot add camera input to session")
            return
        }

        // Create metadata output for QR code detection
        val metadataOutput = AVCaptureMetadataOutput()
        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)
            metadataOutput.setMetadataObjectsDelegate(this, queue = dispatch_get_main_queue())
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        } else {
            onError("Cannot add metadata output to session")
            return
        }

        // Create preview layer
        val preview = AVCaptureVideoPreviewLayer(session = session)
        preview.videoGravity = AVLayerVideoGravityResizeAspectFill
        preview.frame = view.bounds
        view.layer.addSublayer(preview)

        this.captureSession = session
        this.previewLayer = preview
    }

    private fun startSession() {
        captureSession?.let { session ->
            if (!session.isRunning()) {
                dispatch_async(dispatch_get_main_queue()) {
                    session.startRunning()
                    isSessionRunning = true
                }
            }
        }
    }

    private fun stopSession() {
        captureSession?.let { session ->
            if (session.isRunning()) {
                session.stopRunning()
                isSessionRunning = false
            }
        }
    }

    fun resetDetection() {
        hasDetectedCode = false
    }

    // AVCaptureMetadataOutputObjectsDelegate
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        if (hasDetectedCode) return

        for (metadataObject in didOutputMetadataObjects) {
            if (metadataObject is AVMetadataMachineReadableCodeObject) {
                val stringValue = metadataObject.stringValue
                if (stringValue != null && stringValue.isNotEmpty()) {
                    hasDetectedCode = true

                    // Vibrate to indicate successful scan
                    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)

                    onQrCodeDetected(stringValue)
                    break
                }
            }
        }
    }

    fun setTorchEnabled(enabled: Boolean) {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return
        if (!device.hasTorch) return

        try {
            device.lockForConfiguration(null)
            device.torchMode = if (enabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
            device.unlockForConfiguration()
        } catch (e: Exception) {
            // Ignore torch errors
        }
    }
}
