package haffman;

import java.io.File;
import java.nio.charset.MalformedInputException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.io.EOFException;
public class Main {
    private static final byte ENCODING_TABLE_SIZE = 127;

    public static void main(String[] args) throws IOException {

        String stringPath = "C:/Users/SEM/IdeaProjects/Haffman/src/haffman/haff.txt";
        compress(stringPath);



        StringBuilder s = new StringBuilder();
        s.append(stringPath).append(".table.txt");
        String stringTable = s.toString();

        StringBuilder s1 = new StringBuilder();
        s1.append(stringPath).append(".cpr");
        String stringCompressed = s1.toString();
//        Thread.sleep(1000);
        //extract(stringCompressed , stringTable);
    }

    public static void compress(String stringPath) throws IOException {
        List<String> stringList;
        File inputFile = new File(stringPath);
        String s = "";
        File compressedFile, table;

        try {
            stringList = Files.readAllLines(Paths.get(inputFile.getAbsolutePath()));
        } catch (NoSuchFileException e) {
            System.out.println("Неверный путь, или такого файла не существует!");
            return;
        } catch (MalformedInputException e) {
            System.out.println("Текущая кодировка файла не поддерживается");
            return;
        }

        for (String item : stringList) {
            s += item;
            s += '\n';
        }

        HuffmanOperator operator = new HuffmanOperator(new HuffmanTree(s));

        compressedFile = new File(inputFile.getAbsolutePath() + ".cpr");
        compressedFile.createNewFile();
        try (FileOutputHelper fo = new FileOutputHelper(compressedFile)) {
            fo.writeBytes(operator.getBytedMsg());
        }
        //create file with encoding table:

        table = new File(inputFile.getAbsolutePath() + ".table.txt");
        table.createNewFile();
        try (FileOutputHelper fo = new FileOutputHelper(table)) {
            fo.writeString(operator.getEncodingTable());
        }

        System.out.println("Путь к сжатому файлу: " + compressedFile.getAbsolutePath());
        System.out.println("Путь к кодировочной таблице " + table.getAbsolutePath());
        System.out.println("Без таблицы файл будет невозможно извлечь!");

        double idealRatio = Math.round(operator.getCompressionRatio() * 100) / (double) 100;//идеализированный коэффициент
        double realRatio = Math.round((double) inputFile.length()
                / ((double) compressedFile.length() + (double) table.length()) * 100) / (double)100;//настоящий коэффициент

        System.out.println("Идеализированный коэффициент сжатия равен " + idealRatio);
        System.out.println("Коэффициент сжатия с учетом кодировочной таблицы " + realRatio);
    }

    public static void extract(String filePath, String tablePath) throws FileNotFoundException, IOException {
        HuffmanOperator operator = new HuffmanOperator();
        File compressedFile = new File(filePath),
                tableFile = new File(tablePath),
                extractedFile = new File(filePath + ".xtr");
        String compressed = "";
        String[] encodingArray = new String[ENCODING_TABLE_SIZE];
        //read compressed file
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!check here:
        try (FileInputHelper fi = new FileInputHelper(compressedFile)) {
            byte b;
            while (true) {
                b = fi.readByte();//method returns EOFException
                compressed += String.format("%8s", Integer.toBinaryString(b & 0xff)).replace(" ", "0");
            }
        } catch (EOFException e) {

        }

        //--------------------

        //read encoding table:
        try (FileInputHelper fi = new FileInputHelper(tableFile)) {
            fi.readLine();//skip first empty string
            encodingArray[(byte)'\n'] = fi.readLine();//read code for '\n'
            while (true) {
                String s = fi.readLine();
                if (s == null)
                    throw new EOFException();
                encodingArray[(byte)s.charAt(0)] = s.substring(1, s.length());
            }
        } catch (EOFException ignore) {}

        extractedFile.createNewFile();
        //extract:
        try (FileOutputHelper fo = new FileOutputHelper(extractedFile)) {
            fo.writeString(operator.extract(compressed, encodingArray));
        }

        System.out.println("Путь к распакованному файлу " + extractedFile.getAbsolutePath());
    }
}

