package bsi.passwordWallet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptionTest {
    Encryption encryption;

    @Before
    public void setUp() {
        encryption = new Encryption();
    }

    @Test
    public void calculateSHA512_ReturnsHash_IfDataValid() {
        String calculatedHash = encryption.calculateSHA512("testinput", "testsalt");
        String expectedHash =
                "hGcVOneY/UR2LaPHNawSFpgq23fL2MQdZd33Gp90kKAzPp"
                + "XCIsKcedY5k3S8tK3xCuIw9H12/xldibTiEx2pFw==";

        assertEquals(expectedHash, calculatedHash);
    }

    @Test
    public void calculateHMAC_ReturnsHash_IfDataValid() {
        String calculatedHash = encryption.calculateHMAC("testinput", "testsalt");
        String expectedHash =
                "WXRTP8K+6QGuevBqWeMbrC8rxZun2JoBgOm5FQ4xzON2n"
                 + "JMYqFimXnyttxILGY2DwpsPxf90QHJBrkBmj+/Npw==";

        assertEquals(expectedHash, calculatedHash);
    }

    @Test
    public void encryptAES128_ReturnsEncryptedData_IfDataValid() {
        String calculatedCipher = encryption.encryptAES128(
                "testinput", "testkeytestkeyte".getBytes(), "testivtesttestIV".getBytes()
        );

        String expectedCipher = "Uctqj5osVdvO+ThT9nmkLw==";

        assertEquals(expectedCipher, calculatedCipher);
    }

    @Test
    public void decryptAES128_ReturnsDecryptedData_IfDataValid() {
        String decrypted = encryption.encryptAES128(
                "testencrypted", "testkeytestkeyte".getBytes(), "testivtesttestIV".getBytes()
        );

        String expectedText = "NrIe7Qlz02I7SH0olkvc8A==";

        assertEquals(expectedText, decrypted);
    }

}