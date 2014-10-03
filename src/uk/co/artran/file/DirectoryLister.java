package uk.co.artran.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class DirectoryLister {
    public static final String PATH = "/Users/ray/tempFiles";
    public static final int NUMBER_TO_MAKE = 500000;

    private void makeFiles(String path, int numberToMake) throws IOException {
        final File parentDir = new File(path);

        for (int i = 0; i < numberToMake; i++) {
            final File file = new File(parentDir, "temp" + i);
            file.createNewFile();
        }
    }

    private void cleanFiles(String path, int numberToMake) throws IOException {
        final File parentDir = new File(path);

        for (int i = 0; i < numberToMake; i++) {
            final File file = new File(parentDir, "temp" + i);
            file.delete();
        }
    }

    private long directoryWithFilenameFilter(String path) {
        final long start = System.currentTimeMillis();

        final File parentDir = new File(path);
        File[] files = new File[0];
        if (parentDir.isDirectory()) {
            files = parentDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("temp\\d+");
                }
            });
        }
        System.err.println(files[0].getAbsolutePath());

        final long end = System.currentTimeMillis();
        return end - start;
    }

    private long directoryWithFileFilter(String path) {
        final long start = System.currentTimeMillis();

        final File parentDir = new File(path);
        File[] files = new File[0];
        if (parentDir.isDirectory()) {
            files = parentDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().matches("temp\\d+");
                }
            });
        }
        System.err.println(files[0].getAbsolutePath());

        final long end = System.currentTimeMillis();
        return end - start;
    }

    private long directoryWithFileVisitor(String pathStr) throws IOException {
        final long start = System.currentTimeMillis();

        final Path path = FileSystems.getDefault().getPath(pathStr);

        Files.walkFileTree(path, new FileVisitor<Path>() {
            private File result;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final File file1 = file.toFile();
                if (file1.getName().matches("temp\\d+")) {
                    setResult(file1);
                    return TERMINATE;
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return CONTINUE;
            }
        });

        final long end = System.currentTimeMillis();
        return end - start;
    }

    private void setResult(File file) {
        System.err.println(file.getAbsolutePath());
    }

    private long directoryWithOutFilter(String path) {
        final long start = System.currentTimeMillis();

        final File parentDir = new File(path);
        File[] files = new File[0];
        if (parentDir.isDirectory()) {
            files = parentDir.listFiles();
        }
        if (files[0].getName().matches("temp\\d+")) {
            System.err.println(files[0].getAbsolutePath());
        }

        final long end = System.currentTimeMillis();
        return end - start;
    }

    public static void main(String[] args) throws IOException {
        final DirectoryLister directoryLister = new DirectoryLister();

        directoryLister.makeFiles(PATH, NUMBER_TO_MAKE);
        System.err.println(String.format("%d files created", NUMBER_TO_MAKE));

        final long timeWithFileFilter = directoryLister.directoryWithFileFilter(PATH);
        final long timeWithFilenameFilter = directoryLister.directoryWithFilenameFilter(PATH);
        final long timeWithFileVisitor = directoryLister.directoryWithFileVisitor(PATH);
        final long timeWithoutFilter = directoryLister.directoryWithOutFilter(PATH);

        System.err.println("File cleanup starting");
        directoryLister.cleanFiles(PATH, NUMBER_TO_MAKE);

        System.err.println();
        System.err.println("With file filter:     " + timeWithFileFilter + " mSec");
        System.err.println("With filename filter: " + timeWithFilenameFilter + " mSec");
        System.err.println("With file visitor:    " + timeWithFileVisitor + " mSec");
        System.err.println("Without filter:       " + timeWithoutFilter + " mSec");
    }
}
