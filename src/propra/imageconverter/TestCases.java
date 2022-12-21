package propra.imageconverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static propra.imageconverter.ImageConverterUtil.readFile;

//This whole class is only for testing purposes
public class TestCases {
    //this function takes arguments and tests multiple conversions
    private static void testsFromArgs(String[] args) throws IOException {
        ImageConverterUtil util = new ImageConverterUtil();

        switch (args[0]) {
            case "--hin" -> {
                String inputFileName = "../KE2_TestBilder/test_01_uncompressed.tga";
                String outputFileName = "../KE2_Konvertiert/test_01.propra";
                byte[] buf = readFile(inputFileName);
                util.convert(inputFileName, outputFileName, buf, "rle");

                //rle propra to uncompressed tga
                byte[] buf2 = readFile("../KE2_Konvertiert/test_01_rle.propra");
                util.convert("../KE2_Konvertiert/test_01_rle.propra", "../KE2_Konvertiert/test_01_back.tga", buf2, "uncompressed");

                byte[] bytesNewFile = readFile("../KE2_Konvertiert/test_01_back.tga");

                System.out.println("file1 len: " + buf.length);
                System.out.println("file2 len: " + bytesNewFile.length);

                //Look at 10/11 y-Koordinate

                for (int i = 0; i < buf.length; i++) {
                    if (buf[i] != bytesNewFile[i]) {
                        System.out.println("i: " + i);
                        System.out.println("buf: " + buf[i]);
                        System.out.println("new: " + bytesNewFile[i]);
                    }
                }

                System.out.println(Arrays.equals(buf, bytesNewFile));
            }
            case "--her" -> {
                String inputFileName = "../KE2_TestBilder/test_02_rle.tga";
                String outputFileName = "../KE2_Konvertiert/test_02.propra";
                byte[] buf = readFile(inputFileName);
                util.convert(inputFileName, outputFileName, buf, "uncompressed");

                //propra uncompressed to rle tga
                byte[] buf2 = readFile("../KE2_Konvertiert/test_02.propra");
                util.convert("../KE2_Konvertiert/test_02.propra", "../KE2_Konvertiert/test_02_back.tga", buf2, "rle");
            }
            case "test-encoding" -> testEncoding();
        }

        System.exit(0);
    }

    private static void testEncodingBaseN1() {
        String alphabet = "01";

        String[][] testCases = {
                {"f", "01100110"},
                {"fo", "0110011001101111"},
                {"foo", "011001100110111101101111"},
                {"foob", "01100110011011110110111101100010"},
                {"fooba", "0110011001101111011011110110001001100001"},
                {"foobar", "011001100110111101101111011000100110000101110010"},
        };

        for (String[] testCase : testCases) {
            byte[] testBytes = testCase[0].getBytes();
            byte[] str = Encoder.encodeN(testBytes, 2, alphabet);
            String encoded = new String(str, StandardCharsets.UTF_8);

            if (!encoded.equals(testCase[1])) {
                System.err.println("testCase Encoded: " + testCase[0]);
                System.err.println("expected: " + testCase[1]);
                System.err.println("actual: " + encoded);
            }
        }

        for (String[] testCase : testCases) {
            //CPN
            byte[] testBytes = testCase[1].getBytes();
            //expected
            byte[] expected = testCase[0].getBytes();
            //fooba
            byte[] decoded = Encoder.decodeN(testBytes, 2, alphabet);

            if (!Arrays.equals(expected, decoded)) {
                String str = new String(decoded, StandardCharsets.UTF_8);
                System.err.println("testCase decoded: " + testCase[1]);
                System.err.println("expected: " + testCase[0]);
                System.err.println("actual: " + str);
            }
        }
    }

    private static void testEncodingBaseN2() {
        String alphabet = "0123456789ABCDEF";

        String[][] testCases = {
                {"f", "66"},
                {"fo", "666F"},
                {"foo", "666F6F"},
                {"foob", "666F6F62"},
                {"fooba", "666F6F6261"},
                {"foobar", "666F6F626172"},
        };

        for (String[] testCase : testCases) {
            byte[] testBytes = testCase[0].getBytes();
            byte[] str = Encoder.encodeN(testBytes, 16, alphabet);
            String encoded = new String(str, StandardCharsets.UTF_8);

            if (!encoded.equals(testCase[1])) {
                System.err.println("testCase Encoded: " + testCase[0]);
                System.err.println("expected: " + testCase[1]);
                System.err.println("actual: " + encoded);
            }
        }

        for (String[] testCase : testCases) {
            //CPN
            byte[] testBytes = testCase[1].getBytes();
            //expected
            byte[] expected = testCase[0].getBytes();
            //fooba
            byte[] decoded = Encoder.decodeN(testBytes, 16, alphabet);

            if (!Arrays.equals(expected, decoded)) {
                String str = new String(decoded, StandardCharsets.UTF_8);
                System.err.println("testCase decoded: " + testCase[1]);
                System.err.println("expected: " + testCase[0]);
                System.err.println("actual: " + str);
            }
        }
    }

    private static void testEncodingBaseN3() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        String[][] testCases = {
                {"f", "Zg"},
                {"fo", "Zm8"},
                {"foo", "Zm9v"},
                {"foob", "Zm9vYg"},
                {"fooba", "Zm9vYmE"},
                {"foobar", "Zm9vYmFy"},
        };

        for (String[] testCase : testCases) {
            byte[] testBytes = testCase[0].getBytes();
            byte[] str = Encoder.encodeN(testBytes, 64, alphabet);
            String encoded = new String(str, StandardCharsets.UTF_8);

            if (!encoded.equals(testCase[1])) {
                System.err.println("testCase Encoded: " + testCase[0]);
                System.err.println("expected: " + testCase[1]);
                System.err.println("actual: " + encoded);
            }
        }

        for (String[] testCase : testCases) {
            //CPN
            byte[] testBytes = testCase[1].getBytes();
            //expected
            byte[] expected = testCase[0].getBytes();
            //fooba
            byte[] decoded = Encoder.decodeN(testBytes, 64, alphabet);

            if (!Arrays.equals(expected, decoded)) {
                String str = new String(decoded, StandardCharsets.UTF_8);
                System.err.println("testCase decoded: " + testCase[1]);
                System.err.println("expected: " + testCase[0]);
                System.err.println("actual: " + str);
            }
        }
    }

    //this function tests the encoding and decoding of the test images
    private static void testEncoding() throws IOException {
        String encodeBase32 = "--encode-base-32";
        //test 1
        String test_01 = "../KE2_TestBilder/test_05_base32.tga.base-32";
        Encoder.decode(test_01, encodeBase32);
        Encoder.encode("../KE2_Konvertiert/test_05_base32.tga", encodeBase32);

        compareFiles(test_01, "../KE2_Konvertiert/test_05_base32.tga.base-32");

        //test 2
        /*
        String test_02 = "../KE2_TestBilder/test_06_base32.propra.base-32";
        Encoder.decode(test_02, encodeBase32);
        Encoder.encode("../KE2_Konvertiert/test_06_base32.propra", encodeBase32);

        compareFiles(test_02, "../KE2_Konvertiert/test_06_base32.propra.base-32");

        //test 3
        String test_03 = "../KE2_TestBilder/test_02_rle.tga";
        Encoder.encode(test_03, encodeBase32);
        Encoder.decode("../KE2_Konvertiert/test_02_rle.tga.base-32", encodeBase32);

        compareFiles(test_03, "../KE2_Konvertiert/test_02_rle.tga");

        //test 4
        String test_04 = "../KE2_TestBilder/test_04_rle.propra";
        Encoder.encode(test_04, encodeBase32);
        Encoder.decode("../KE2_Konvertiert/test_04_rle.propra.base-32", encodeBase32);

        compareFiles(test_04, "../KE2_Konvertiert/test_04_rle.propra");

         */
    }

    //this function checks file equality
    private static void compareFiles(String inFile, String outFile) throws IOException {
        byte[] in = readFile(inFile);
        byte[] out = readFile(outFile);

        System.out.println("images equal: " + Arrays.equals(in, out));
        int count = 0;

        for (int i = 0; i < in.length; i++) {
            if (in[i] != out[i]) {
                count++;
                System.out.println("i: " + i);
                System.out.println("in: " + in[i]);
                System.out.println("out: " + out[i]);
            }
        }
        System.out.println(in.length);
        System.out.println(out.length);

        System.out.println("count" + count);
    }

    public static void main(String[] args) throws IOException {
        //testEncodingBaseN1();
        //testEncodingBaseN2();
        //testEncodingBaseN3();
        //testsFromArgs(new String[]{"test-encoding"});

        /*PropraConverter converter = new PropraConverter();
        //converter.testStreaming("../KE1_TestBilder/test_03_uncompressed.propra", "../KE2_Konvertiert/test_03.tga");

        ImageConverterUtil util = new ImageConverterUtil();

        converter.convertToTga(
                ImageConverterUtil.readFile(
                        "../KE1_TestBilder/test_03_uncompressed.propra"
                ),
                "../KE2_Konvertiert/test_03_nostreaming.tga"
        );

        compareFiles("../KE2_Konvertiert/test_03.tga", "../KE2_Konvertiert/test_03_nostreaming.tga");*/

        String input = "--input=../KE3_TestBilder/test_05_huffman.propra";

        ImageConverterUtil util = new ImageConverterUtil();
        String inputFileName = input.replace("--input=", "");

        String output = "--output=../KE3_Konvertiert/test_05.tga";

        String compressionArg = "--compression=rle";

        String outputFileName = output.replace("--output=", "");

        String compression = compressionArg.replace("--compression=", "");

        byte[] buf = readFile(inputFileName);
        util.convert(inputFileName, outputFileName, buf, compression);

        /*PropraConverter propraConverter = new PropraConverter();

        PropraConverter.Tree tree = propraConverter.createHuffmannTree();

        tree.printPreorder(tree.root);*/
    }

    //this function tests the encoding and decoding
    public void testCasesEncoding() {
        String[][] testCases = {
                {"f", "CO"},
                {"fo", "CPNG"},
                {"foo", "CPNMU"},
                {"foob", "CPNMUOG"},
                {"fooba", "CPNMUOJ1"},
                {"foobar", "CPNMUOJ1E8"},
        };

        for (String[] testCase : testCases) {
            byte[] testBytes = testCase[0].getBytes();
            byte[] str = Encoder.encodeBase32(testBytes);
            String encoded = new String(str, StandardCharsets.UTF_8);

            if (!encoded.equals(testCase[1])) {
                System.err.println("testCase Encoded: " + testCase[0]);
                System.err.println("expected: " + testCase[1]);
                System.err.println("actual: " + encoded);
            }
        }

        for (String[] testCase : testCases) {
            //CPN
            byte[] testBytes = testCase[1].getBytes();
            //expected
            byte[] expected = testCase[0].getBytes();
            //fooba
            byte[] decoded = Encoder.decodeBase32(testBytes);

            if (!Arrays.equals(expected, decoded)) {
                String str = new String(decoded, StandardCharsets.UTF_8);
                System.err.println("testCase decoded: " + testCase[1]);
                System.err.println("expected: " + testCase[0]);
                System.err.println("actual: " + str);
            }
        }
    }

    //this function tests the checkSum calculation
    private void testCases(Converter converter) {
        String[][] t = {
                {"t", "0x00750076"},
                {"te", "0x00DC0152"},
                {"tes", "0x015202A4"},
                {"test", "0x01CA046E"},
                {"Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.", "0x3C4EEB4C"},
                {"Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", "0x079ED65E"}
        };

        for (String[] s : t) {
            byte[] test = s[0].getBytes();
            long cur = converter.calcCheckSum(test);
            System.out.println("hex: " + Long.toHexString(cur));
            System.out.println("expected: " + s[1]);
        }

        byte[][] te = {
                {},
                {0},
                {1},
                {0, 1},
                {1, 0},
                {(byte) 255, (byte) 128}
        };
        String[] hex = {"0x00000001", "0x00010002", "0x00020003", "0x00040006", "0x00040007", "0x01820283"};

        for (int i = 0; i < te.length; i++) {
            long cur = converter.calcCheckSum(te[i]);
            System.out.println("hex: " + Long.toHexString(cur));
            System.out.println("expected: " + hex[i]);
        }
    }
}
