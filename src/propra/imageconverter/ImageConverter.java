package propra.imageconverter;

import java.io.File;
import java.io.IOException;

import static propra.imageconverter.ImageConverterUtil.readFile;

public class ImageConverter {

    public static void main(String[] args) throws IOException {
        String input = args[0];

        File testFile = new File("");
        String currentPath = testFile.getAbsolutePath();
        System.out.println("current path is: " + currentPath);

        ImageConverterUtil util = new ImageConverterUtil();
        String inputFileName = input.replace("--input=", "");

        if (args[1].startsWith("--encode-base")) {
            Encoder.encode(inputFileName, args[1]);
        } else if (args[1].startsWith("--decode-base")) {
            Encoder.decode(inputFileName, args[1]);
        } else {
            String output = args[1];

            String compressionArg;

            if (args.length == 3) {
                compressionArg = args[2];
            } else {
                compressionArg = "uncompressed";
            }

            String outputFileName = output.replace("--output=", "");

            String compression = compressionArg.replace("--compression=", "");

            byte[] buf = readFile(inputFileName);
            util.convert(inputFileName, outputFileName, buf, compression);
        }
    }
}

