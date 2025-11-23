import { Component, EventEmitter, Input, Output, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { BarcodeFormat } from '@zxing/library';
import { ZXingScannerModule } from '@zxing/ngx-scanner';

@Component({
  selector: 'app-barcode-scanner',
  standalone: true,
  imports: [CommonModule, MatIconModule, ZXingScannerModule],
  templateUrl: './barcode-scanner.component.html',
  styleUrls: ['./barcode-scanner.component.scss']
})
export class BarcodeScannerComponent implements OnInit, OnDestroy {
  @Output() barcodeDetected = new EventEmitter<string>();
  @Output() permissionDenied = new EventEmitter<void>();
  @Output() camerasFound = new EventEmitter<MediaDeviceInfo[]>();

  allowedFormats = [
    BarcodeFormat.EAN_13,
    BarcodeFormat.EAN_8,
    BarcodeFormat.CODE_128,
    BarcodeFormat.QR_CODE
  ];

  hasDevices: boolean = false;
  hasPermission: boolean = false;
  availableDevices: MediaDeviceInfo[] = [];
  currentDevice: MediaDeviceInfo | undefined;
  torchEnabled: boolean = false;
  torchAvailable: boolean = false;

  ngOnInit(): void {
    // Component initialization
  }

  ngOnDestroy(): void {
    // Cleanup if needed
  }

  onCamerasFound(devices: MediaDeviceInfo[]): void {
    this.availableDevices = devices;
    this.hasDevices = devices && devices.length > 0;
    this.camerasFound.emit(devices);

    // Select back camera by default (environment facing)
    const backCamera = devices.find(device =>
      /back|rear|environment/i.test(device.label)
    );

    this.currentDevice = backCamera || devices[0];
  }

  onCodeResult(resultString: string): void {
    // Haptic feedback (vibration)
    if (navigator.vibrate) {
      navigator.vibrate(100);
    }

    // Play beep sound
    this.playBeep();

    // Emit the detected barcode
    this.barcodeDetected.emit(resultString);
  }

  onHasPermission(has: boolean): void {
    this.hasPermission = has;
    if (!has) {
      this.permissionDenied.emit();
    }
  }

  onTorchCompatible(compatible: boolean): void {
    this.torchAvailable = compatible;
  }

  toggleCamera(): void {
    if (this.availableDevices.length > 1) {
      const currentIndex = this.availableDevices.indexOf(this.currentDevice!);
      const nextIndex = (currentIndex + 1) % this.availableDevices.length;
      this.currentDevice = this.availableDevices[nextIndex];
    }
  }

  toggleTorch(): void {
    this.torchEnabled = !this.torchEnabled;
  }

  private playBeep(): void {
    try {
      const audioContext = new AudioContext();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      oscillator.type = 'sine';
      oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);

      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.1);

      // Cleanup
      setTimeout(() => {
        audioContext.close();
      }, 200);
    } catch (error) {
      // Silently fail if audio context is not supported
      console.warn('Audio feedback not available:', error);
    }
  }
}
