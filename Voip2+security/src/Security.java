package src.networksassignment;

import java.nio.ByteBuffer;

public class Security//Diffie-Hellman Key exchange
{
    int publicPrime;
    long publicLargeNum;
    int privateNum;
    long sharedKey;
    byte[] keyArray;

    public Security(int newPrivateNum, int newPublicPrime, long newPublicLargeNum)
    {
        privateNum = newPrivateNum;
        publicPrime = newPublicPrime;
        publicLargeNum = newPublicLargeNum;
    }

    public Security(long newSharedKey)//Purely for testing purposes
    {
        sharedKey = newSharedKey;
        byte[] array = ByteBuffer.allocate(8).putLong(sharedKey).array();
        byte[] keys = new byte[512];
        int count = 0;
        for (int i = 0; i < 512; i++)//For every other iteration of putting the key into the array, it will perform bitwise not on each byte
        {
            for (int j = 0; j < array.length; j++)
            {
                if (count % 2 == 0)
                {
                    keys[i] = array[j];
                    i++;
                }
                else
                {
                    keys[i] = (byte) ~array[j];
                    i++;
                }
            }
            count++;
            i--;
        }
        keyArray = keys;
    }

    public long FirstStep()
    {
        long res = 1L;
        for (int i = 1; i <= privateNum; i++)//Doing public prime num raised by private key
        {
            res = res * publicPrime;
        }
        return res % publicLargeNum;
    }

    public void GenerateSharedKey(long theirNum)
    {
        long res = 1L;
        for (int i = 1; i <= privateNum; i++)//Doing their calculated num raised by private key
        {
            res = res * theirNum;
        }
        sharedKey = res % publicLargeNum;
        byte[] array = ByteBuffer.allocate(8).putLong(sharedKey).array();
        byte[] keys = new byte[512];
        int count = 0;
        for (int i = 0; i < 512; i++)
        {
            for (int j = 0; j < array.length; j++)//For every other iteration of putting the key into the array, it will perform bitwise not on each byte
            {
                if (count % 2 == 0)
                {
                    keys[i] = array[j];
                    i++;
                }
                else
                {
                    keys[i] = (byte) ~array[j];
                    i++;
                }
            }
            count++;
            i--;
        }
        keyArray = keys;
    }

    public boolean Authenticate(byte[] recording)
    {
        long total = 0;
        long hash = 0;
        for (int i = 0; i < recording.length - 8; i++)
        {
            total = total + recording[i];
        }
        for (int i = recording.length - 8; i < recording.length; i++)
        {
            hash = (hash << 8) + (recording[i] & 0xFF);
        }
        return (total % sharedKey == hash);
    }

    /*public byte[] Encypt(byte[] recording)//Backup plan
    {
        ByteBuffer intRepresentation = ByteBuffer.allocate(recording.length * 2);
        for(int i = 0; i < recording.length; i++)
        {
            //recording[i] = (byte) (recording[i] ^ sharedKey);
            short byteNum = recording[i];
            short encryptedNum = (short) (byteNum * sharedKey);
            intRepresentation.putShort(encryptedNum);
        }
        return intRepresentation.array();
    }*/

    /*public byte[] Decrypt(byte[] recording)//Backup plan decryption
    {
        ByteBuffer backToByte = ByteBuffer.allocate(recording.length / 2);
        for(int i = 0; i < recording.length; i++)
        {
            //recording[i] = (byte) (recording[i] ^ sharedKey);
            int value = 0;
            for (int j = i; j < i + 2; j++)
            {
                value = (value << 8) + (recording[j] & 0xFF);
            }
            i = i + 1;
            short num = (short) (value / sharedKey);
            byte num2 = (byte) num;
            backToByte.put(num2);
        }
        return backToByte.array();
    }*/

    public byte[] Encrypt(byte[] recording)
    {
        ByteBuffer full = ByteBuffer.allocate(recording.length + 8);
        byte[] encrypted = new byte[512];
        int total = 0;
        for(int i = 0; i < recording.length; i++)
        {
            byte encryptedByte = recording[i];
            encryptedByte = (byte) (encryptedByte ^ keyArray[i]);
            encrypted[i] = encryptedByte;
            total = total + encryptedByte;
        }
        long hash = total % sharedKey;
        full.put(encrypted);
        full.putLong(hash);
        return full.array();
    }

    public byte[] Decrypt(byte[] recording)
    {
        byte[] encrypted = new byte[512];
        //long total = 0;
        for(int i = 0; i < recording.length - 8; i++)
        {
            byte encryptedByte = recording[i];
            encryptedByte = (byte) (encryptedByte ^ keyArray[i]);
            encrypted[i] = encryptedByte;
            //total = total + encryptedByte;
        }
        return encrypted;
    }

    /*public byte[] EncryptXOR(byte[] recording)
    {
        /*BigInteger num = new BigInteger(recording);
        BigInteger key = BigInteger.valueOf(sharedKey);
        BigInteger result = num.xor(key);
        byte[] x = result.toByteArray();
        if(x.length != 32) 
        {
            byte[] finalKey= new byte[512];
            byte a = Byte.parseByte("00000000", 2);
            for(int i =0; i<x.length; i++) 
                finalKey[i] = x[i];
            for (int i = x.length ; i<512 ; i++)
                finalKey[i] = a;
            x = finalKey;
        }
        return x;
        ByteBuffer keyArray = ByteBuffer.allocate(512);
        for(int i = 0; i < 512/8; i++)
        {
            keyArray.putLong(sharedKey);
        }
        byte[] key = keyArray.array();
        byte[] encrypted = new byte[512];
        for(int i = 0; i < recording.length; i++)
        {
            encrypted[i] = (byte) (recording[i] ^ key[i]);
        }
        return encrypted;
    }*/

    public static void main(String[] args)
    {
        //Security x = new Security(5, 7, 100);
        //Security y = new Security(12, 7, 100);
        //long x1 = x.FirstStep();
        //long y1 = y.FirstStep();
        //x.GenerateSharedKey(y1);
        //y.GenerateSharedKey(x1);
        //System.out.println("yes");
    }
}