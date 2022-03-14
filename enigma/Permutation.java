package enigma;

import java.util.ArrayList;
import java.util.HashMap;

import static enigma.EnigmaException.*;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author Bradley Tian
 */
class Permutation {

    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) throws EnigmaException {
        for (char s : forbiddenChars) {
            if (alphabet.contains(s)) {
                throw error("alphabet contains "
                        + "illegal characters.");
            }
        }
        _alphabet = alphabet;
        cycleCollection = new ArrayList<>();
        boolean extract = false;
        int index = -1;

        for (int i = 0; i < cycles.length(); i++) {
            char current = cycles.charAt(i);
            if (current == ')') {
                extract = false;
            } else if (extract) {
                if (_alphabet.contains(current)) {
                    cycleCollection.set(index, cycleCollection.get(index)
                            + cycles.charAt(i));
                } else {
                    throw error("element in cycle not in alphabet.");
                }
            } else if (cycles.charAt(i) == '(') {
                extract = true;
                cycleCollection.add("");
                index++;
            }
        }

        permutations = new HashMap<>();

        for (int i = 0; i < cycleCollection.size(); i++) {
            addCycle(cycleCollection.get(i));
        }

        if (permutations.keySet().size() < _alphabet.size()) {
            for (int i = 0; i < _alphabet.size(); i++) {
                if (!permutations.containsKey(_alphabet.toChar(i))) {
                    permutations.put(_alphabet.toChar(i), _alphabet.toChar(i));
                }
            }
        }
    }

    /**
     * Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     * c0c1...cm.
     */
    private void addCycle(String cycle) throws EnigmaException {
        char firstElement = cycle.charAt(0);
        if (permutations.containsKey(firstElement)) {
            throw error("duplicate permutation detected.");
        } else {
            char last = firstElement;

            for (int i = 1; i < cycle.length(); i++) {
                if (permutations.containsKey(cycle.charAt(i))) {
                    throw error("duplicate permutation detected.");
                } else {
                    permutations.put(last, cycle.charAt(i));
                    last = cycle.charAt(i);
                }
            }

            permutations.put(last, firstElement);
        }
    }

    /**
     * Return the value of P modulo the size of this permutation.
     */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    int size() {
        return _alphabet.size();
    }

    /**
     * Return the result of applying this permutation to P modulo the
     * alphabet size.
     */
    int permute(int p) throws EnigmaException {
        int apply = wrap(p);
        if (apply < 0 || apply >= _alphabet.size()) {
            throw error("invalid permuting position.");
        }
        return _alphabet.toInt(permute(_alphabet.toChar(apply)));
    }

    /**
     * Return the result of applying the inverse of this permutation
     * to  C modulo the alphabet size.
     */
    int invert(int c) throws EnigmaException {
        int apply = wrap(c);
        if (apply < 0 || apply >= _alphabet.size()) {
            throw error("invalid permuting position.");
        }
        return _alphabet.toInt(invert(_alphabet.toChar(apply)));
    }

    /**
     * Return the result of applying this permutation to the index of P
     * in ALPHABET, and converting the result to a character of ALPHABET.
     */
    char permute(char p) throws EnigmaException {
        if (!_alphabet.contains(p)) {
            throw error("character not in alphabet.");
        } else {
            return permutations.get(p);
        }
    }

    /**
     * Return the result of applying the inverse of this permutation to C.
     */
    char invert(char c) throws EnigmaException {
        for (char key : permutations.keySet()) {
            if (permutations.get(key) == c) {
                return key;
            }
        }
        throw error("character not in alphabet.");
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Return true iff this permutation is a derangement (i.e., a
     * permutation for which no value maps to itself).
     */
    boolean derangement() {
        return false;
    }

    /**
     * Alphabet of this permutation.
     */
    private Alphabet _alphabet;

    /** List of forbidden alphabet characters. */
    private char[] forbiddenChars = {'(', ')', '*'};

    /** Collection of permutations. */
    private ArrayList<String> cycleCollection;

    /** Representation of the permuted wiring. */
    private HashMap<Character, Character> permutations;
}
