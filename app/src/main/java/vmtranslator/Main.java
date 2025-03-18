/*
 * Runs the main
 */
package vmtranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InvalidCommandException {
        if (args.length == 0) {
            System.out.println("Please provide a file or directory path as argument");
            return;
        }
        String path = args[0];
        File source = new File(path);

        if (!source.exists()) {
            System.out.println("The provided path does not exist.");
            return;
        }

        if (source.isFile() && isAllowedFile(source)) {
            String outputFilename = path.substring(0, path.length() - 3) + ".asm";
            CodeWriter cw = new CodeWriter(outputFilename);
            processFile(source, cw);
            cw.close();
        } else if (source.isDirectory()) {
            String outputFilename = path + ".asm";
            CodeWriter cw = new CodeWriter(outputFilename);
            cw.writeInit();
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isAllowedFile(file)) {
                        processFile(file, cw);
                    }
                }
            }
            cw.close();

        } else {
            System.out.println("Unsupported input. Please provide a valid file or directory");
            return;
        }
    }

    private static void translate(CodeWriter cw, Parser parser)
            throws NullPointerException, IOException, InvalidCommandException {
        try {
            while (parser.hasMoreCommands()) {
                parser.advance();
                switch (parser.commandType()) {
                    case CommandType.C_ARITHMETIC:
                        cw.writeArithmetic(parser.arg1());
                        break;
                    case CommandType.C_PUSH:
                        cw.writePushPop("push", parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_POP:
                        cw.writePushPop("pop", parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_LABEL:
                        cw.writeLabel(parser.arg1());
                        break;
                    case CommandType.C_GOTO:
                        cw.writeGoto(parser.arg1());
                        break;
                    case CommandType.C_IF:
                        cw.writeIf(parser.arg1());
                        break;
                    case CommandType.C_FUNCTION:
                        cw.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_CALL:
                        cw.writeCall(parser.arg1(), parser.arg2());
                        break;
                    case CommandType.C_RETURN:
                        cw.writeReturn();
                        break;
                    default:
                        System.out.println("Command not supported. Skipping:" + parser.commandType().toString());
                }
            }
            parser.close();
        } catch (FileNotFoundException fne) {
            System.out.print("File not exists");
            fne.printStackTrace();
        } finally {
            parser.close();
        }
    }

    private static boolean isAllowedFile(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".vm")) {
            return true;
        } else {
            return false;
        }
    }

    private static void processFile(File source, CodeWriter cw) {
        String sourceFileName = source.getName();
        String name = sourceFileName.substring(0, sourceFileName.length() - 3);
        try {
            cw.setVmFilename(name);
            Parser parser = new Parser(source);
            translate(cw, parser);
        } catch (Exception e) {
            System.out.println("Exception occured");
            e.printStackTrace();
        }
    }
}
