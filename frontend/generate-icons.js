const fs = require('fs');
const path = require('path');

// Simple PNG placeholder generator (1x1 blue pixel)
// This is a minimal PNG file with a single blue pixel
const createPNG = (size) => {
  // PNG signature and minimal IHDR, IDAT, IEND chunks for a 1x1 blue pixel
  const pngSignature = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);

  // IHDR chunk (width=size, height=size, 8-bit, RGB)
  const ihdr = Buffer.concat([
    Buffer.from([0x00, 0x00, 0x00, 0x0D]), // Length
    Buffer.from('IHDR'),
    Buffer.from([
      (size >> 24) & 0xFF, (size >> 16) & 0xFF, (size >> 8) & 0xFF, size & 0xFF, // Width
      (size >> 24) & 0xFF, (size >> 16) & 0xFF, (size >> 8) & 0xFF, size & 0xFF, // Height
      0x08, // Bit depth
      0x02, // Color type (RGB)
      0x00, // Compression
      0x00, // Filter
      0x00  // Interlace
    ]),
    Buffer.from([0xA3, 0x5E, 0x8B, 0xD4]) // CRC
  ]);

  // Simple IDAT chunk with blue color
  const idat = Buffer.from([
    0x00, 0x00, 0x00, 0x0C, // Length
    0x49, 0x44, 0x41, 0x54, // "IDAT"
    0x08, 0x99, 0x63, 0x60, 0x18, 0x05, 0xA3, 0x60, 0x14, 0x8C, 0x02, 0x08,
    0x00, 0x00, 0x04, 0x10, 0x00, 0x01
  ]);

  // IEND chunk
  const iend = Buffer.from([
    0x00, 0x00, 0x00, 0x00, // Length
    0x49, 0x45, 0x4E, 0x44, // "IEND"
    0xAE, 0x42, 0x60, 0x82  // CRC
  ]);

  return Buffer.concat([pngSignature, ihdr, idat, iend]);
};

const sizes = [72, 96, 128, 144, 152, 192, 384, 512];
const iconsDir = path.join(__dirname, 'src', 'assets', 'icons');

if (!fs.existsSync(iconsDir)) {
  fs.mkdirSync(iconsDir, { recursive: true });
}

sizes.forEach(size => {
  const filename = `icon-${size}x${size}.png`;
  const filepath = path.join(iconsDir, filename);
  const png = createPNG(size);
  fs.writeFileSync(filepath, png);
  console.log(`Created ${filename}`);
});

console.log('All icon placeholders created successfully!');
