package propra.imageconverter;

import java.io.*;

abstract class Converter {
    final int TGA_HEADER_SIZE = 18;
    final int PROPRA_HEADER_SIZE = 30;

    final int HEADER_DIF = PROPRA_HEADER_SIZE - TGA_HEADER_SIZE;

    final int[] TGA_WIDTH = {12, 13};
    final int[] PROPRA_WIDTH = {13, 14};

    final int[] TGA_HEIGHT = {14, 15};
    final int[] PROPRA_HEIGHT = {15, 16};

    final byte TGA_TYPE_INDEX = 2;
    final byte PROPRA_TYPE_INDEX = 12;

    final byte TGA_UNCROMPRESSED = 2;
    final byte PROPRA_UNCROMPRESSED = 0;

    final byte TGA_RLE = 10;
    final byte PROPRA_RLE = 1;

    final byte TGA_BIT_PER_POINT_INDEX = 16;
    final byte PROPRA_BIT_PER_POINT_INDEX = 17;

    abstract void convert(byte[] buf, String outPutFileName, String outputFileExtension, String outputCompression) throws FileNotFoundException, IOException;

    abstract void checkDimension(byte[] buf) throws Exception;

    abstract void checkCompression(byte[] buf) throws Exception;

    abstract void checkMissingData(byte[] buf) throws Exception;

    protected void WriteFile(byte[] output, String outputFileName) throws IOException {
        File outputFile = new File(outputFileName);
        outputFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(output);
        outputStream.close();
        System.out.println("Wrote File");
    }

    protected void checkDimension(byte b0, byte b1) throws Exception {
        int dimension = this.getDimension(b0, b1);

        if (dimension == 0) {
            throw new Exception("invalid dimension");
        }
    }

    protected int getDimension(byte b0, byte b1) {
        return Byte.toUnsignedInt(b0) | Byte.toUnsignedInt(b1) << 8;
    }

    protected long calcCheckSum(byte[] buf) {
        long Ais[] = this.calcAn(buf.length, buf);
        long An = 0;
        if (Ais.length != 0) {
            An = Ais[Ais.length - 1] % 65521;
        }

        long Bn = this.calcB(buf, Ais);

        long P = (long) ((An * Math.pow(2, 16)) + Bn);
        return P;
    }

    private long[] calcAn(int Ai, byte[] buf) {
        final int X = 65521;

        long[] Ais = new long[Ai];

        long sum = 0;

        for (int i = 0; i < Ai; i++) {
            sum += (i + 1 + Byte.toUnsignedInt(buf[i]));
            Ais[i] = sum;
        }

        return Ais;
    }

    protected long calcB(byte[] buf, long[] Ais) {
        long b = 1;

        for (int i = 0; i < buf.length; i++) {
            b = (b + (Ais[i] % 65521)) % 65521;
        }

        return b;
    }

    protected byte[] deCompressRle(byte[] buf) {
        //uncompress buf
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < buf.length - 3; i = i + 4) {
            byte counterByte = buf[i];

            byte b = buf[i + 1];
            byte g = buf[i + 2];
            byte r = buf[i + 3];

            int repetitionCount = Byte.toUnsignedInt(counterByte) - 128;

            if (repetitionCount >= 0) {
                for (int j = 0; j <= repetitionCount; j++) {
                    out.write(b);
                    out.write(g);
                    out.write(r);
                }
            } else {
                //number raw Pixel
                int numPix = Byte.toUnsignedInt(counterByte);
                for (int j = 0; j <= numPix; j++) {
                    if (j == 0) {
                        out.write(b);
                        out.write(g);
                        out.write(r);
                    } else {
                        byte b1 = buf[i + (j * 3) + 1];
                        byte g1 = buf[i + (j * 3) + 2];
                        byte r1 = buf[i + (j * 3) + 3];
                        out.write(b1);
                        out.write(g1);
                        out.write(r1);
                    }
                }

                i = i + numPix * 3;
            }
        }

        return out.toByteArray();
    }

    protected byte[] compressRle(byte[] uncrompressedData) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < uncrompressedData.length - 2; i = i + 3) {
            byte b = uncrompressedData[i];
            byte g = uncrompressedData[i + 1];
            byte r = uncrompressedData[i + 2];

            int counter = 0;
            for (; i < uncrompressedData.length - 5; i = i + 3) {
                byte b1 = uncrompressedData[i + 3];
                byte g1 = uncrompressedData[i + 4];
                byte r1 = uncrompressedData[i + 5];

                if (counter == 127) {
                    break;
                }

                if (b == b1 && g == g1 && r == r1) {
                    counter++;
                } else {
                    break;
                }
            }

            int c = counter + 128;

            out.write((byte) c);
            out.write(b);
            out.write(g);
            out.write(r);
        }

        return out.toByteArray();
    }
}
