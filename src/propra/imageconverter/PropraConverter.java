package propra.imageconverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PropraConverter extends Converter {
    private static final byte PROPRA_HUFFMANN = 2;

    @Override
    void convert(byte[] buf, String outPutFileName, String outPutFileExtension, String outputCompression) throws IOException {
        Compression inputCompression = Compression.UNCOMPRESSED;
        if (buf[PROPRA_TYPE_INDEX] == PROPRA_RLE) {
            inputCompression = Compression.RLE;
        } else if (buf[PROPRA_TYPE_INDEX] == PROPRA_HUFFMANN) {
            inputCompression = Compression.HUFFMANN;
        }

        if (outPutFileExtension.equals("tga")) {
            //input uncompressed
            if (inputCompression == Compression.UNCOMPRESSED) {
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertToTga(buf, outPutFileName);
                } else if (outputCompression.equals(Compression.RLE.label)) {
                    this.convertUncompressedToRleTga(buf, outPutFileName);
                }
            } else if (inputCompression == Compression.RLE) {
                //input rle
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertRlePropraToUncompressedTga(buf, outPutFileName);
                } else if (outputCompression.equals("rle")) {
                    this.convertRlePropraToRleTga(buf, outPutFileName);
                }
            } else {
                //input huffmann
                //convert to uncompressed tga
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertHuffMannToUncompressedTga(buf, outPutFileName);
                } else if (outputCompression.equals("rle")) {
                    //convert to
                    this.convertHuffMannPropraToRleTga(buf, outPutFileName);
                }
            }

        } else if (outPutFileExtension.equals("propra")) {
            if (inputCompression == Compression.UNCOMPRESSED) {
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertToPropra(buf, outPutFileName);
                } else if (outputCompression.equals(Compression.RLE.label)) {
                    this.convertUncompressedPropraToRlePropra(buf, outPutFileName);
                }
            } else {
                //input rle
                if (outputCompression.equals(Compression.UNCOMPRESSED.label)) {
                    this.convertRlePropraToUncompressedPropra(buf, outPutFileName);
                } else if (outputCompression.equals("rle")) {
                    this.convertToPropra(buf, outPutFileName);
                }
            }
        } else {
            System.err.println("unknown output extension");
            System.exit(123);
        }
    }

    private byte[] deCompressHuffmann(byte[] buf){
        //uncompress buf
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HuffmannTree tree = new HuffmannTree();
        int i = 1;


        boolean[] myBitSet = new boolean[buf.length * 8];

        for(int j = 0; j < buf.length; j++){
            byte curB = buf[j];

            int b1 = (curB >> 7) & 1;
            int b2 = (curB >> 6) & 1;
            int b3 = (curB >> 5) & 1;
            int b4 = (curB >> 4) & 1;
            int b5 = (curB >> 3) & 1;
            int b6 = (curB >> 2) & 1;
            int b7 = (curB >> 1) & 1;
            int b8 = curB & 1;

            myBitSet[j * 8] = (b1 == 1);
            myBitSet[j * 8 + 1] = (b2 == 1);
            myBitSet[j * 8 + 2]  = (b3 == 1);
            myBitSet[j * 8 + 3] = (b4 == 1);
            myBitSet[j * 8 + 4] = (b5 == 1);
            myBitSet[j * 8 + 5] = (b6== 1);
            myBitSet[j * 8 + 6] = (b7 == 1);
            myBitSet[j * 8 + 7] = (b8 == 1);
        }


        int counter = 0;

        //construct tree
        for(; i < myBitSet.length; i++){

            boolean j = myBitSet[i];
            HuffmannNode newNode;

            if(j){
                //Blatt
                //read next byte
                //read next 8 bits and convert to byte

                byte val = 0;

                val += (myBitSet[i + 1] ? 1 : 0) << 7;
                val += (myBitSet[i + 2] ? 1 : 0) << 6;
                val += (myBitSet[i + 3] ? 1 : 0) << 5;
                val += (myBitSet[i + 4] ? 1 : 0) << 4;
                val += (myBitSet[i + 5] ? 1 : 0) << 3;
                val += (myBitSet[i + 6] ? 1 : 0) << 2;
                val += (myBitSet[i + 7] ? 1 : 0) << 1;
                val += (myBitSet[i + 8] ? 1 : 0);


                newNode = new HuffmannNode(val);
                newNode.isBlatt = true;

                if(!tree.insert(newNode)){
                    System.out.println("over" + i);
                    break;
                }else {
                    counter++;
                    i = i + 8;
                };
            } else {
                newNode = new HuffmannNode((byte)0);
                if(!tree.insert(newNode)){

                    System.out.println("over" + i);
                    break;
                }
            }
        }

        HuffmannNode cur = tree.root;

        for(; i < myBitSet.length; i++){
            boolean curBit = myBitSet[i];

            if(curBit){
                cur = cur.right;
            }else {
                cur = cur.left;
            }

            if(cur.isBlatt){
                //write it
                out.write(cur.key);
                cur = tree.root;
            }
        }

        return out.toByteArray();
    }

    private void convertHuffMannPropraToRleTga(byte[] buf, String outPutFileName) throws IOException {
        byte[] unCompressedImageData = this.deCompressHuffmann(Arrays.copyOfRange(buf, PROPRA_HEADER_SIZE, buf.length));
        byte[] uncompressedPropra = new byte[PROPRA_HEADER_SIZE + unCompressedImageData.length];

        System.arraycopy(buf, 0, uncompressedPropra, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(unCompressedImageData, 0, uncompressedPropra, PROPRA_HEADER_SIZE, unCompressedImageData.length);

        this.convertUncompressedToRleTga(uncompressedPropra, outPutFileName);
    }

    private void convertHuffMannToUncompressedTga(byte[] buf, String outPutFileName) throws IOException {
        byte[] unCompressedImageData = this.deCompressHuffmann(Arrays.copyOfRange(buf, PROPRA_HEADER_SIZE, buf.length));
        byte[] uncompressedPropra = new byte[PROPRA_HEADER_SIZE + unCompressedImageData.length];

        System.out.println("uncompressedImageData " + unCompressedImageData.length);

        System.arraycopy(buf, 0, uncompressedPropra, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(unCompressedImageData, 0, uncompressedPropra, PROPRA_HEADER_SIZE, unCompressedImageData.length);

        this.convertToTga(uncompressedPropra, outPutFileName);
    }

    private void convertRlePropraToUncompressedPropra(byte[] buf, String outPutFileName) throws IOException {
        byte[] propraHeader = Arrays.copyOfRange(buf, 0, PROPRA_HEADER_SIZE);
        byte[] decompressedImageData = this.deCompressRle(Arrays.copyOfRange(buf, PROPRA_HEADER_SIZE, buf.length));

        byte[] rlePropra = new byte[decompressedImageData.length + propraHeader.length];

        System.arraycopy(rlePropra, 0, propraHeader, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(rlePropra, 0, rlePropra, PROPRA_HEADER_SIZE, decompressedImageData.length);


        this.WriteFile(rlePropra, outPutFileName);
    }

    private void convertUncompressedPropraToRlePropra(byte[] buf, String outPutFileName) throws IOException {
        byte[] propraHeader = Arrays.copyOfRange(buf, 0, PROPRA_HEADER_SIZE);
        byte[] compressedImageData = this.compressRle(Arrays.copyOfRange(buf, PROPRA_HEADER_SIZE, buf.length));

        byte[] rlePropra = new byte[compressedImageData.length + propraHeader.length];

        System.arraycopy(rlePropra, 0, propraHeader, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(rlePropra, 0, rlePropra, PROPRA_HEADER_SIZE, compressedImageData.length);

        this.WriteFile(rlePropra, outPutFileName);
    }

    private void convertRlePropraToRleTga(byte[] buf, String outPutFileName) throws IOException {
        byte[] unCompressedImageData = this.deCompressRle(Arrays.copyOfRange(buf, PROPRA_HEADER_SIZE, buf.length));
        byte[] uncompressedPropra = new byte[PROPRA_HEADER_SIZE + unCompressedImageData.length];

        System.arraycopy(buf, 0, uncompressedPropra, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(unCompressedImageData, 0, uncompressedPropra, PROPRA_HEADER_SIZE, unCompressedImageData.length);

        this.convertUncompressedToRleTga(uncompressedPropra, outPutFileName);
    }

    private void convertRlePropraToUncompressedTga(byte[] buf, String outPutFileName) throws IOException {
        byte[] unCompressedImageData = this.deCompressRle(Arrays.copyOfRange(buf, PROPRA_HEADER_SIZE, buf.length));
        byte[] uncompressedPropra = new byte[PROPRA_HEADER_SIZE + unCompressedImageData.length];

        System.arraycopy(buf, 0, uncompressedPropra, 0, PROPRA_HEADER_SIZE);
        System.arraycopy(unCompressedImageData, 0, uncompressedPropra, PROPRA_HEADER_SIZE, unCompressedImageData.length);

        this.convertToTga(uncompressedPropra, outPutFileName);
    }

    private void convertUncompressedToRleTga(byte[] buf, String outPutFileName) throws IOException {
        byte[] uncompressedTga = this.initializeUncompressedTga(buf);
        uncompressedTga[2] = TGA_RLE;

        byte[] compressedWord = this.compressRle(Arrays.copyOfRange(uncompressedTga, TGA_HEADER_SIZE, uncompressedTga.length));
        byte[] compressedTga = new byte[TGA_HEADER_SIZE + compressedWord.length];

        System.arraycopy(uncompressedTga, 0, compressedTga, 0, TGA_HEADER_SIZE);
        System.arraycopy(compressedWord, 0, compressedTga, TGA_HEADER_SIZE, compressedWord.length);

        this.WriteFile(compressedTga, outPutFileName);
    }

    public void convertToTga(byte[] buf, String outPutFileName) throws IOException {
        byte[] outputBytes = this.initializeUncompressedTga(buf);

        this.WriteFile(outputBytes, outPutFileName);
    }

    private void convertToPropra(byte[] buf, String outPutFileName) throws IOException {
        this.WriteFile(buf, outPutFileName);
    }

    private int sizeFromByteArray(byte[] buf) {
        ByteBuffer sizeBuffer = ByteBuffer.wrap(Arrays.copyOfRange(buf, 18, 26));
        sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return sizeBuffer.getInt();
    }

    private int getSize(byte[] buf) {
        int width = this.getDimension(buf[PROPRA_WIDTH[0]], buf[PROPRA_WIDTH[1]]);
        int height = this.getDimension(buf[PROPRA_HEIGHT[0]], buf[PROPRA_HEIGHT[1]]);
        int bitperpixel = buf[PROPRA_BIT_PER_POINT_INDEX];

        return (width * height * (bitperpixel / 8));
    }


    private byte[] initializeUncompressedTga(byte[] buf) {
        int size = this.getSize(buf);

        System.out.println("size: " + size);
        System.out.println("buf length: " + buf.length);

        byte[] outputBytes = new byte[size + TGA_HEADER_SIZE];
        System.out.println("output size: " + outputBytes.length);

        for (int i = 0; i < TGA_HEADER_SIZE; i++) {
            outputBytes[i] = 0;
        }

        // compression_type
        outputBytes[2] = TGA_UNCROMPRESSED;

        // width
        outputBytes[TGA_WIDTH[0]] = buf[PROPRA_WIDTH[0]];
        outputBytes[TGA_WIDTH[1]] = buf[PROPRA_WIDTH[1]];

        // height
        outputBytes[TGA_HEIGHT[0]] = buf[PROPRA_HEIGHT[0]];
        outputBytes[TGA_HEIGHT[1]] = buf[PROPRA_HEIGHT[1]];

        // Bits per image point
        outputBytes[16] = 0x18;

        outputBytes[17] = 32;

        for (int i = TGA_HEADER_SIZE; i < outputBytes.length - 2; i = i + 3) {
            // b
            outputBytes[i] = buf[i + HEADER_DIF + 1];
            // g
            outputBytes[i + 1] = buf[i + HEADER_DIF + 2];
            // r
            outputBytes[i + 2] = buf[i + HEADER_DIF];
        }

        return outputBytes;
    }

    @Override
    void checkDimension(byte[] buf) throws Exception {
        this.checkDimension(buf[PROPRA_WIDTH[0]], buf[PROPRA_WIDTH[1]]);
        this.checkDimension(buf[PROPRA_HEIGHT[0]], buf[PROPRA_HEIGHT[1]]);
    }

    @Override
    void checkCompression(byte[] buf) throws Exception {
        if (buf[this.PROPRA_TYPE_INDEX] != PROPRA_UNCROMPRESSED) {
            throw new Exception("invalid compression type");
        }
    }

    @Override
    void checkMissingData(byte[] buf) throws Exception {
        int size = this.sizeFromByteArray(buf);

        if (buf.length != size + PROPRA_HEADER_SIZE) {
            System.out.println("size: " + size);
            System.out.println("buf length: " + buf.length);
            throw new Exception("missing data: buffer length smaller than given size plus header");
        }
    }

    protected void checkCheckSum(byte[] buf) {
        ByteBuffer checkSumBuffer = ByteBuffer.wrap(Arrays.copyOfRange(buf, 26, 30));
        checkSumBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int intCheckSumHeader = checkSumBuffer.getInt();
        long checkSumHeader = Integer.toUnsignedLong(intCheckSumHeader);
        long checkSum = this.calcCheckSum(Arrays.copyOfRange(buf, 30, buf.length));
        System.out.println("checkSum of Header: " + checkSumHeader);
        System.out.println("checkSum calculated: " + checkSum);

        if (checkSum != checkSumHeader) {
            System.err.println("calculated checkSum is not equal to calculated checksum");
            System.exit(123);
        }
    }
}
