package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static enigma.TestUtils.UPPER_STRING;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class. For the purposes of
 * this lab (in order to test) this is an abstract class, but in proj1, it will
 * be a concrete class. If you want to copy your tests for proj1, you can make
 * this class concrete by removing the 4 abstract keywords and implementing the
 * 3 abstract methods.
 *
 *  @author Bradley Tian
 */
public class PermutationTest {

    Permutation getNewPermutation(String cycles, Alphabet alphabet) {
        return new Permutation(cycles, alphabet);
    }

    Alphabet getNewAlphabet(String chars) {
        return new Alphabet(chars);
    }

    Alphabet getNewAlphabet() {
        return new Alphabet();
    }

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha,
                           Permutation perm, Alphabet alpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.toInt(c), ei = alpha.toInt(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        Alphabet alpha = getNewAlphabet();
        Permutation perm = getNewPermutation("", alpha);
        checkPerm("identity", UPPER_STRING, UPPER_STRING, perm, alpha);
    }

    @Test
    public void testInvertChar() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals('B', p.invert('A'));
        assertEquals('D', p.invert('B'));
    }

    @Test
    public void testInvertPermuteInt() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        assertEquals(1, p.invert(0));
        assertEquals(0, p.permute(1));
        assertEquals(2, p.invert(3));
        assertEquals(0, p.permute(1));
        assertEquals(3, p.invert(5));
        assertEquals(2, p.invert(-1));
    }


    @Test(expected = EnigmaException.class)
    public void charNotInAlphaTests() {
        Permutation p = getNewPermutation("(BACD)", getNewAlphabet("ABCD"));
        p.invert('F');
    }

    @Test
    public void identityTests() {
        Alphabet a = getNewAlphabet("ABCD");
        Permutation p = getNewPermutation("(C)", a);
        checkPerm("identity", "ABCD", "ABCD", p, a);
    }

    @Test
    public void singleCycleTests() {
        Alphabet a = getNewAlphabet("ABCDEFGHI");
        Permutation p = getNewPermutation("(ABCDEF)", a);
        checkPerm("single", "ABCDEFGHI", "BCDEFAGHI", p, a);
    }

    @Test
    public void multiCycleTests() {
        Alphabet a = getNewAlphabet("AZBYCT");
        Permutation p = getNewPermutation("(AZBY) (CT)", a);
        checkPerm("multi", "ACYZBT", "ZTABYC", p, a);
        assertEquals('T', p.invert('C'));
        assertEquals('A', p.permute('Y'));
    }

    @Test(expected = EnigmaException.class)
    public void repeatedCycleTests() {
        Alphabet a = getNewAlphabet("AZBYC");
        Permutation p = getNewPermutation("(AZBY) (CA)", a);
        p.permute('C');
    }
}
