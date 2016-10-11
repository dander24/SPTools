package com.company;

import org.apache.commons.io.*;
import org.apache.commons.lang3.*;

import java.io.*;
import java.util.ArrayList;

/*
repackage functionality excluded due to java not handling raw data as well as it should
script.dat needs to be manually re-inserted into the dat file and the related pointers adjusted
-d -- decrypts the the .dat
-e -- recrypts the .dat
-v -- verifies script format
-f -- formats the script for editing
-r -- reformats a script to be used by normal humans
 */


public class Main {
    private static byte[] decryptedBytes;

    public static void main(String[] args) {
        try {
            File file = new File(args[1]);
            decryptedBytes = loadFile(file);
            if (args[0].equals("-d")) {
                decrypt(decryptedBytes);
                FileOutputStream fout = new FileOutputStream("decrypted.dat", false);
                fout.write(decryptedBytes);
            }
            if (args[0].equals("-e")) {
                encrypt(decryptedBytes);
                FileOutputStream fout = new FileOutputStream("recrypted.dat", false);
                fout.write(decryptedBytes);
            }
            if (args[0].equals("-v")) {
                verify(decryptedBytes);
            }
            if (args[0].equals("-f")) {
                FileOutputStream fout = new FileOutputStream("script.txt", false);
                fout.write((format(decryptedBytes)));
            }
            if (args[0].equals("-r")) {
                FileOutputStream fout = new FileOutputStream("reformatted.dat", false);
                fout.write((reformat(decryptedBytes)));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] format(byte[] bytes) {
        int curbyte = 0;
        int maxbyte = bytes.length;
        ArrayList<Byte> script = new ArrayList<>();

        // ++ 0x0D 0x0A
        while (curbyte < maxbyte - 1)
        {
            try {
                if (bytes[curbyte] == 0x00) {
                    script.add((byte) 0x0D);
                    script.add((byte) 0x0A);
                    curbyte += 5;
                } else {
                    script.add(bytes[curbyte]);
                    curbyte += 1;
                }
            }
            catch (Exception e)
            {
                curbyte +=1;
            }
        }

        Byte[] returnarr = script.toArray(new Byte[script.size()]);
        return ArrayUtils.toPrimitive(returnarr);
    }

    private static byte[] reformat(byte[] bytes)
    {
        int curbyte = 0;
        int maxbytes = bytes.length;
        byte b1 = 0x00;
        byte b2 = 0x00;
        byte b3 = 0x00;
        ArrayList<Byte> script = new ArrayList<>();

        while (curbyte < maxbytes ) {

            if(bytes[curbyte] == 0x0D && bytes[curbyte+1] == 0x0A)
            {
                script.add((byte) 0x00);
                script.add(b1);
                script.add(b2);
                script.add(b3);
                script.add((byte) 0x00);

                if (b1 == -1) {
                    if (b2 == -1) {
                        b1 = 0x00;
                        b2 = 0x00;
                        b3 += 0x01;
                    } else {
                        b1 = 0x00;
                        b2 += 0x01;
                    }
                } else {
                    b1 += 0x01;
                }
                curbyte += 2;
            }
            else
            {
                script.add(bytes[curbyte]);
                curbyte += 1;
            }

        }

        script.add((byte) 0x00);
        Byte[] returnarr = script.toArray(new Byte[script.size()]);
        return ArrayUtils.toPrimitive(returnarr);
    }

    private static byte[] loadFile(File file) {
        try {
            InputStream fin = new FileInputStream(file);
            return IOUtils.toByteArray(fin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte rotateLeft(byte bits, int shift) {
        return (byte) (((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
    }


    private static void decrypt(byte[] bytes) {
        int encodedLength = bytes.length;
        byte shift = 4;

        for (int i = 16; i < encodedLength; i += 4) {
            byte[] block = new byte[4];
            shift &= 7;
            block[0] = bytes[i];
            block[1] = bytes[i + 1];
            block[2] = bytes[i + 2];
            block[3] = bytes[i + 3];
            block[0] = rotateLeft(block[0], shift);
            bytes[i] = (byte) (block[0] ^ 0x9D);
            bytes[i + 1] = (byte) (block[1] ^ 0x85);
            bytes[i + 2] = (byte) (block[2] ^ 0xD5);
            bytes[i + 3] = (byte) (block[3] ^ 0xF7);
            shift++;

        }
    }


    private static void encrypt(byte[] bytes) {
        int encodedLength = bytes.length;
        byte shift = 4;

        for (int i = 16; i < encodedLength; i += 4) {
            byte[] block = new byte[4];
            shift &= 7;
            block[0] = bytes[i];
            block[1] = bytes[i + 1];
            block[2] = bytes[i + 2];
            block[3] = bytes[i + 3];
            bytes[i] = (byte) (block[0] ^ 0x9D);
            bytes[i + 1] = (byte) (block[1] ^ 0x85);
            bytes[i + 2] = (byte) (block[2] ^ 0xD5);
            bytes[i + 3] = (byte) (block[3] ^ 0xF7);
            bytes[i] = rotateLeft(bytes[i], shift);
            shift--;

        }
    }

    private static void verify(byte[] bytes) {
        int curbyte = 0;
        int maxbytes = bytes.length;
        byte b1 = 0x00;
        byte b2 = 0x00;
        byte b3 = 0x00;
        byte nb1, nb2, nb3;

        while (curbyte < maxbytes) {
            if (bytes[curbyte] == 0x00) {
                try {
                    nb1 = bytes[curbyte + 1];
                    nb2 = bytes[curbyte + 2];
                    nb3 = bytes[curbyte + 3];

                    if (b1 != nb1) {
                        throw new IOException();
                    }

                    curbyte += 1;

                    if (b2 != nb2) {
                        throw new IOException();
                    }

                    curbyte += 1;

                    if (b3 != nb3) {
                        throw new IOException();
                    }

                    if (b1 == -1) {
                        if (b2 == -1) {
                            b1 = 0x00;
                            b2 = 0x00;
                            b3 += 0x01;
                        } else {
                            b1 = 0x00;
                            b2 += 0x01;
                        }
                    } else {
                        b1 += 0x01;
                    }

                    curbyte += 3;
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (bytes.length <= curbyte + 1) {
                        System.out.println("test passed");
                        curbyte += 1;
                        break;
                    } else {
                        System.out.println("Index OOB error @ " + curbyte + " verify integrity of file");
                        curbyte += 1;
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("data mismatch at byte " + curbyte);
                    break;
                }
            } else {
                curbyte += 1;
            }
        }

    }

}
