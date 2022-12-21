package propra.imageconverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class TgaConverter extends Converter {
    final byte[] format = {80, 114, 111, 80, 114, 97, 87, 105, 83, 101, 50, 50};

    @Override
    void convert(byte[] buf, String outPutFileName, String outputFileExtension, String outputCompression) throws IOException {
        Compression inputCompression = Compression.UNCOMPRESSED;
        if (buf[2] == TGA_RLE) {
            inputCompression = Compression.RLE;
        }

        if (outputFileExtension.equals("propra")) {
            //input uncompressed
            if (inputCompression == Compression.UNCOMPRESSED) {
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertUncompressedTgaToUncompressedPropra(buf, outPutFileName);
                } else if (outputCompression.equals(Compression.RLE.label)) {
                    this.convertUncompressedTgaToRLEPropra(buf, outPutFileName);
                }
            } else {
                //input rle
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertRleTgaToUncompressedPropra(buf, outPutFileName);
                } else if (outputCompression.equals(Compression.RLE.label)) {
                    this.convertRleTgaToRlePropra(buf, outPutFileName);
                }
            }
        } else if (outputFileExtension.equals("tga")) {
            // input uncompressed
            if (inputCompression == Compression.UNCOMPRESSED) {
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertToTga(buf, outPutFileName);
                } else if (outputCompression.equals(Compression.RLE.label)) {
                    this.convertUncompressedTgaToRleTga(buf, outPutFileName);
                }
            } else {
                //input rle
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertRleTgaToUncompressedTga(buf, outPutFileName);
                } else if (outputCompression.equals(Compression.RLE.label)) {
                    this.convertToTga(buf, outPutFileName);
                }
            }
            this.convertToTga(buf, outPutFileName);
        } else {
            System.err.println("unknown output image type");
            System.exit(123);
        }
    }

    private void convertRleTgaToUncompressedTga(byte[] buf, String outPutFileName) throws IOException {
        //decompress tga data
        byte[] decompressedData = this.deCompressRle(Arrays.copyOfRange(buf, TGA_HEADER_SIZE, buf.length));

        // create uncompressed tga
        byte[] uncompressedTga = new byte[TGA_HEADER_SIZE + decompressedData.length];
        System.arraycopy(buf, 0, uncompressedTga, 0, TGA_HEADER_SIZE);
        System.arraycopy(decompressedData, 0, uncompressedTga, TGA_HEADER_SIZE, decompressedData.length);

        this.convertToTga(uncompressedTga, outPutFileName);
    }

    private void convertUncompressedTgaToRleTga(byte[] buf, String outPutFileName) throws IOException {
        //compress tga data
        byte[] compressedData = this.compressRle(Arrays.copyOfRange(buf, TGA_HEADER_SIZE, buf.length));

        //create rle tga
        byte[] rleTga = new byte[TGA_HEADER_SIZE + compressedData.length];
        System.arraycopy(buf, 0, rleTga, 0, TGA_HEADER_SIZE);
        System.arraycopy(compressedData, 0, rleTga, TGA_HEADER_SIZE, compressedData.length);

        this.convertToTga(rleTga, outPutFileName);
    }

    private void convertRleTgaToRlePropra(byte[] buf, String outPutFileName) throws IOException {
        //uncompress rle imagedata
        byte[] imageData = this.deCompressRle(Arrays.copyOfRange(buf, TGA_HEADER_SIZE, buf.length));

        //create uncompressed propra
        byte[] uncompressedPropra = new byte[TGA_HEADER_SIZE + imageData.length];
        System.arraycopy(buf, 0, uncompressedPropra, 0, TGA_HEADER_SIZE);
        System.arraycopy(imageData, 0, uncompressedPropra, TGA_HEADER_SIZE, imageData.length);

        //convert uncompressed tga to rle propra
        this.convertUncompressedTgaToRLEPropra(uncompressedPropra, outPutFileName);
    }


    private void convertRleTgaToUncompressedPropra(byte[] buf, String outPutFileName) throws IOException {
        //uncompress rle tga
        byte[] imageData = this.deCompressRle(Arrays.copyOfRange(buf, TGA_HEADER_SIZE, buf.length));

        //create uncompressedTga
        byte[] uncompressedTga = new byte[TGA_HEADER_SIZE + imageData.length];
        System.arraycopy(buf, 0, uncompressedTga, 0, TGA_HEADER_SIZE);
        System.arraycopy(imageData, 0, uncompressedTga, TGA_HEADER_SIZE, imageData.length);

        //convert uncompressed tga to propra
        this.convertUncompressedTgaToUncompressedPropra(uncompressedTga, outPutFileName);
    }

    private void convertUncompressedTgaToRLEPropra(byte[] buf, String outPutFileName) throws IOException {
        // create uncompressed propra
        byte[] uncompressedPropra = this.initUncompressedPropra(buf);
        uncompressedPropra[PROPRA_TYPE_INDEX] = PROPRA_RLE;

        //compress propra image data to rle
        byte[] imageData = Arrays.copyOfRange(uncompressedPropra, PROPRA_HEADER_SIZE, uncompressedPropra.length);
        byte[] compressedImageData = this.compressRle(imageData);

        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
        sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        sizeBuffer.putInt(compressedImageData.length);
        byte[] sizeBytes = sizeBuffer.array();

        long checksum = this.calcCheckSum(compressedImageData);

        ByteBuffer checkSumBuffer = ByteBuffer.allocate(4);
        checkSumBuffer.order(ByteOrder.LITTLE_ENDIAN);
        checkSumBuffer.putInt((int) (checksum));

        byte[] checkSumArr = checkSumBuffer.array();
        System.out.println("calculated checkSum: " + checksum);


        //construct compressed propra bytes
        byte[] compressedPropra = new byte[PROPRA_HEADER_SIZE + compressedImageData.length];

        System.arraycopy(uncompressedPropra, 0, compressedPropra, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(sizeBytes, 0, compressedPropra, 18, 8);
        System.arraycopy(checkSumArr, 0, compressedPropra, 26, 4);
        System.arraycopy(compressedImageData, 0, compressedPropra, PROPRA_HEADER_SIZE, compressedImageData.length);

        this.WriteFile(compressedPropra, outPutFileName);
    }

    private int calcIdAndPaletteOffset(byte[] buf) {
        int lengthImgId = Byte.toUnsignedInt(buf[0]);

        int paletteType = Byte.toUnsignedInt(buf[1]);

        int numColorsPalette = 0;
        int paletteBits = 0;

        if (paletteType == 1) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(buf[3]);
            buffer.put(buf[4]);
            numColorsPalette = buffer.getShort();

            paletteBits = buf[5] / 8;
        }

        return lengthImgId + numColorsPalette * paletteBits;
    }


    private byte[] initUncompressedPropra(byte[] buf) {
        int size = this.getAmountPixel(buf);
        byte[] outputBytes = new byte[size + PROPRA_HEADER_SIZE];

        for (int i = 0; i < outputBytes.length; i++) {
            outputBytes[i] = 0;
        }

        System.arraycopy(format, 0, outputBytes, 0, format.length);
        outputBytes[PROPRA_TYPE_INDEX] = PROPRA_UNCROMPRESSED;

        // width
        outputBytes[PROPRA_WIDTH[0]] = buf[TGA_WIDTH[0]];
        outputBytes[PROPRA_WIDTH[1]] = buf[TGA_WIDTH[1]];

        // height
        outputBytes[PROPRA_HEIGHT[0]] = buf[TGA_HEIGHT[0]];
        outputBytes[PROPRA_HEIGHT[1]] = buf[TGA_HEIGHT[1]];

        outputBytes[PROPRA_BIT_PER_POINT_INDEX] = 24;

        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
        sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        sizeBuffer.putInt(size);
        byte[] sizeBytes = sizeBuffer.array();

        System.arraycopy(sizeBytes, 0, outputBytes, 18, 8);
        for (int i = PROPRA_HEADER_SIZE; i < outputBytes.length - 2; i = i + 3) {
            // r
            outputBytes[i] = buf[i - HEADER_DIF + 2];
            // b
            outputBytes[i + 1] = buf[i - HEADER_DIF];
            // g
            outputBytes[i + 2] = buf[i - HEADER_DIF + 1];
        }

        return outputBytes;
    }

    private void convertUncompressedTgaToUncompressedPropra(byte[] buf, String outPutFileName) throws IOException {
        byte[] outputBytes = this.initUncompressedPropra(buf);

        long checksum = this.calcCheckSum(Arrays.copyOfRange(outputBytes, PROPRA_HEADER_SIZE, outputBytes.length));

        ByteBuffer checkSumBuffer = ByteBuffer.allocate(4);
        checkSumBuffer.order(ByteOrder.LITTLE_ENDIAN);
        checkSumBuffer.putInt((int) (checksum));

        byte[] checkSumArr = checkSumBuffer.array();
        System.out.println("calculated checkSum: " + checksum);

        System.arraycopy(checkSumArr, 0, outputBytes, 26, 4);

        this.WriteFile(outputBytes, outPutFileName);
    }

    private void convertToTga(byte[] buf, String outPutFileName) throws IOException {
        buf[0] = 0;
        buf[1] = 0;
        buf[2] = TGA_UNCROMPRESSED;
        buf[3] = 0;
        buf[4] = 0;
        buf[5] = 0;

        buf[16] = 0x18;
        buf[17] = 32;

        this.WriteFile(buf, outPutFileName);
    }

    @Override
    void checkDimension(byte[] buf) throws Exception {
        this.checkDimension(buf[TGA_WIDTH[0]], buf[TGA_WIDTH[1]]);
        this.checkDimension(buf[TGA_HEIGHT[0]], buf[TGA_HEIGHT[1]]);
    }

    @Override
    void checkCompression(byte[] buf) throws Exception {
        int compressionType = buf[this.TGA_TYPE_INDEX];
        if (compressionType != TGA_UNCROMPRESSED) {
            System.out.println("compressionType: " + compressionType);
            throw new Exception("invalid compression type");
        }
    }

    @Override
    void checkMissingData(byte[] buf) throws Exception {
        int size = this.getAmountPixel(buf) + TGA_HEADER_SIZE + this.calcIdAndPaletteOffset(buf);

        if (buf[2] == TGA_UNCROMPRESSED) {
            if (buf.length < size) {
                System.out.println("buffer length:" + buf.length);
                System.out.println("expected size" + buf.length);
                throw new Exception("missing data");
            }
        }
    }

    private int getAmountPixel(byte[] buf) {
        int width = this.getDimension(buf[TGA_WIDTH[0]], buf[TGA_WIDTH[1]]);
        int height = this.getDimension(buf[TGA_HEIGHT[0]], buf[TGA_HEIGHT[1]]);
        int bitperpixel = buf[TGA_BIT_PER_POINT_INDEX];

        return (width * height * (bitperpixel / 8));
    }
}
