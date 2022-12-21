package propra.imageconverter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Encoder {
    private static final String base32chars = "0123456789ABCDEFGHIJKLMNOPQRSTUV";

    public static byte[] encodeBase32(byte[] buf) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < buf.length; i = i + 5) {
            long n = 0;
            int j = 0;

            for (; j < 5; j++) {
                if (i + j >= buf.length) {
                    break;
                } else {
                    n += ((long) Byte.toUnsignedInt(buf[i + j]) << (32 - 8 * j));
                }
            }

            double bits = j * 8;
            double cur = Math.ceil(bits / 5);

            for (int x = 0; x < cur; x++) {
                long n1 = (n >> (35 - (5 * x))) & 31;
                out.write(base32chars.charAt((int) n1));
            }
        }

        return out.toByteArray();
    }

    public static byte[] decodeBase32(byte[] buf) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < buf.length; i += 8) {

            long n = 0;
            int j = 0;

            for (; j < 8; j++) {
                if (i + j >= buf.length) {
                    break;
                } else {
                    n += ((long) base32chars.indexOf(buf[i + j]) << (35 - (j * 5)));
                }
            }

            double bits = j * 5;
            double cur = Math.floor(bits / 8);

            for (int x = 0; x < cur; x++) {
                long byte1 = ((n >>> (32 - 8 * x)) & 0xFF);
                out.write((int) byte1);
            }
        }

        return out.toByteArray();
    }

    public static byte[] encodeN(byte[] buf, int n, String alphabet) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //2^x = n
        //x*lg2 = lgn
        //x = lgn/lg2

        //calculate the bits which are encoded
        int bitsEncoded = (int) (Math.log(n) / Math.log(2));

        int commonFactor = bitsEncoded * 8;

        for (int i = 0; i < buf.length; i = i + bitsEncoded) {
            long sum = 0;
            int j = 0;

            for (; j < bitsEncoded; j++) {
                if (i + j >= buf.length) {
                    break;
                } else {
                    sum += ((long) Byte.toUnsignedInt(buf[i + j]) << (commonFactor - 8 * (j + 1)));
                }
            }

            double bits = j * 8;
            double cur = Math.ceil(bits / bitsEncoded);

            for (int x = 0; x < cur; x++) {
                long n1 = (sum >> (commonFactor - (bitsEncoded * (x + 1)))) & (n - 1);
                out.write(alphabet.charAt((int) n1));
            }
        }

        return out.toByteArray();
    }

    public static byte[] decodeN(byte[] buf, int n, String alphabet) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int bitsEncoded = (int) (Math.log(n) / Math.log(2));
        int commonFactor = bitsEncoded * 8;

        for (int i = 0; i < buf.length; i += 8) {

            long sum = 0;
            int j = 0;

            for (; j < 8; j++) {
                if (i + j >= buf.length) {
                    break;
                } else {
                    sum += ((long) alphabet.indexOf(buf[i + j]) << (commonFactor - ((j + 1) * bitsEncoded)));
                }
            }

            double bits = j * bitsEncoded;
            double cur = Math.floor(bits / 8);

            for (int x = 0; x < cur; x++) {
                long byte1 = ((sum >>> (commonFactor - 8 * (x + 1))) & 0xFF);
                out.write((int) byte1);
            }
        }

        return out.toByteArray();
    }


    public static void encode(String inputFileName, String encoding) throws IOException {
        byte[] fileBytes = ImageConverterUtil.readFile(inputFileName);
        byte[] bytesEncoded;
        String outputFileName;
        if (encoding.equals("--encode-base-32")) {
            bytesEncoded = encodeBase32(fileBytes);

            outputFileName = inputFileName + ".base-32";

        } else {
            String[] encodingParams = encoding.split("=");
            int n = Integer.parseInt(encodingParams[0].replace("--encode-base", ""));
            String alphabet = encodingParams[1];

            bytesEncoded = encodeN(fileBytes, n, alphabet);

            outputFileName = inputFileName + ".base-" + n;
        }


        File outputFile = new File(outputFileName);
        outputFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(bytesEncoded);
        outputStream.close();

        System.out.println("Wrote File - encoded");
    }

    public static void decode(String inputFileName, String decoding) throws IOException {
        byte[] imageBytes = ImageConverterUtil.readFile(inputFileName);
        byte[] decoded = {};

        if (decoding.equals("--decode-base-32")) {
            decoded = Encoder.decodeBase32(imageBytes);
        }

        File outputFile = new File(inputFileName.replace(".base-32", ""));
        outputFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(decoded);
        outputStream.close();
        System.out.println("Wrote File - decoded");
    }
}
