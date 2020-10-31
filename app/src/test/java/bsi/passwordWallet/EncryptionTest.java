package bsi.passwordWallet;

import android.util.Log;

import junit.framework.TestCase;

import org.junit.rules.ExpectedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.MacSpi;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class EncryptionTest {

    Encryption encryption;

    @BeforeMethod
    public void setUp() {
        encryption = new Encryption();
    }

//    @DataProvider(name = "randomIVDataProvider")
//    public static Object[][] ivData() {
//        return  new Object[][] {
//                {new byte[]{1}, "[B@22927a81".getBytes(StandardCharsets.US_ASCII)},
//                {new byte[]{-1}, "[B@2c8d66b2".getBytes()},
//                {new byte[]{-56, 120, 0, -121}, "[B@2f7c7260".getBytes()}
//        };
//    }

//    @Test(dataProvider = "randomIVDataProvider")
//    public void randomIV_GeneratesIV_IfSeedSpecified(byte[] seed, byte[] expIV) {
//        byte[] iv = new byte[16];
//        //Encryption.randomIV(new byte[]{-56, 120, 0, -121});
//        //System.out.println(iv);
//        assertArrayEquals(Encryption.randomIV(seed), expIV);
//    }

    @Test
    public void calculateSHA512_CalculatesHash_WhenValidDataPassed() {
        encryption.setMessageDigest(new MessageDigest("") {
            @Override
            protected void engineUpdate(byte input) { }

            @Override
            protected void engineUpdate(byte[] input, int offset, int len) { }

            @Override
            protected byte[] engineDigest() {
                return new byte[64];
            }

            @Override
            protected void engineReset() { }
        });

        String hash = encryption.calculateSHA512("test", "salt", "pepper");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    public void calculateHMAC_CalculatesHash_WhenValidDataPassed() {
        encryption.setMac(new Encryption.MacWrapper() {
            @Override
            public void init(SecretKeySpec keySpec) { }

            @Override
            public void update(byte[] input) { }

            @Override
            public byte[] doFinal(byte[] input) {
                return new byte[64];
            }
        });

        String hash = encryption.calculateHMAC("test", null, "testpepper");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
//
//    @Test(expectedExceptions = InvalidKeyException.class)
//    public void calculateHMAC_ThrowsException_WhenKeyInvalid() {
//        encryption.setMac(new Encryption.MacWrapper() {
//            @Override
//            public void init(SecretKeySpec keySpec) throws InvalidKeyException {
//                throw new InvalidKeyException();
//            }
//
//            @Override
//            public void update(byte[] input) { }
//
//            @Override
//            public byte[] doFinal(byte[] input) {
//                return new byte[64];
//            }
//        });
//
//        String hash = encryption.calculateHMAC("test", null, "testpepper");
//    }

    @Test
    public void calculateMD5_CalculatesHash_WhenValidDataPassed() {
        encryption.setMessageDigest(new MessageDigest("") {
            @Override
            protected void engineUpdate(byte input) { }

            @Override
            protected void engineUpdate(byte[] input, int offset, int len) { }

            @Override
            protected byte[] engineDigest() {
                return new byte[16];
            }

            @Override
            protected void engineReset() { }
        });

        byte[] hash = encryption.calculateMD5("test");
        assertNotNull(hash);
        assertEquals(16, hash.length);
    }

    @Test
    public void encryptAES128_EncryptsInput_WhenValidDataPassed() {
        encryption.setCipher(new Encryption.CipherWrapper() {
            @Override
            public void init(int mode, SecretKeySpec secretKeySpec, IvParameterSpec ivParam) { }

            @Override
            public byte[] doFinal(byte[] input) {
                return new byte[16];
            }
        });

        String cipher = encryption.encryptAES128("testInput123", null, null);
        assertNotNull(cipher);
    }

    @Test
    public void decryptAES128_DecryptsInput_WhenValidDataPassed() {
        encryption.setCipher(new Encryption.CipherWrapper() {
            @Override
            public void init(int mode, SecretKeySpec secretKeySpec, IvParameterSpec ivParam) { }

            @Override
            public byte[] doFinal(byte[] input) {
                return new byte[16];
            }
        });

        String decrypted = encryption.decryptAES128("231gfd3r4", null, null);
        assertNotNull(decrypted);
    }

    @Test
    public void generateSalt64_GeneratesSalt() {
        String salt = Encryption.generateSalt64();
        assertNotNull(salt);
    }

    @Test
    public void randomIV_GeneratesIV() {
        byte[] iv = Encryption.randomIV();
        assertNotNull(iv);
    }
}