package enigma;

import static enigma.EnigmaException.*;

/**
 * Superclass that represents a rotor in the enigma machine.
 *
 * @author Bradley Tian
 */
class Rotor {

    /**
     * A rotor named NAME whose permutation is given by PERM.
     */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _position = 0;
        _notches = "";
        _newNotches = _notches;
    }

    /**
     * Return my name.
     */
    String name() {
        return _name;
    }

    /**
     * Return my alphabet.
     */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /**
     * Return my permutation.
     */
    Permutation permutation() {
        return _permutation;
    }

    /**
     * Return the size of my alphabet.
     */
    int size() {
        return _permutation.size();
    }

    /**
     * Return true iff I have a ratchet and can move.
     */
    boolean rotates() {
        return false;
    }

    /**
     * Return true iff I reflect.
     */
    boolean reflecting() {
        return false;
    }

    /**
     * Return my current setting.
     */
    int setting() {
        return _position;
    }

    /**
     * Set setting() to POSN.
     */
    void set(int posn) throws EnigmaException {
        if (posn < 0 || posn >= _permutation.alphabet().size()) {
            throw error("Position not in alphabet.");
        } else {
            _position = posn;
        }

    }

    /**
     * Set setting() to character CPOSN.
     */
    void set(char cposn) throws EnigmaException {
        if (!_permutation.alphabet().contains(cposn)) {
            throw error("Position not in alphabet.");
        } else {
            _position = _permutation.alphabet().toInt(cposn);
        }
    }

    void reset() {
        _position = 0;
        _newNotches = _notches;
    }

    /**
     * Reset my permutation.
     * @param p The permutation inputted.
     */
    void setPermutation(Permutation p) {
        _permutation = p;
    }

    void setRing(int ring) {
        _position = _permutation.wrap(_position - ring);
        String newNotches = "";
        for (int i = 0; i < _notches.length(); i++) {
            int index = alphabet().toInt(_notches.charAt(i));
            index = _permutation.wrap(index - ring);
            newNotches += alphabet().toChar(index);
        }
        _newNotches = newNotches;
    }

    /**
     * Return the conversion of P (an integer in the range 0..size()-1)
     * according to my permutation.
     */
    int convertForward(int p) {
        int result = _permutation.wrap(_permutation.permute
                (_permutation.wrap(p + _position)) - _position);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return result;
    }

    /**
     * Return the conversion of E (an integer in the range 0..size()-1)
     * according to the inverse of my permutation.
     */
    int convertBackward(int e) {
        int result = _permutation.wrap(_permutation.invert
                (_permutation.wrap(e + _position)) - _position);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return result;
    }

    /**
     * Returns the positions of the notches, as a string giving the letters
     * on the ring at which they occur.
     */
    String notches() {
        return _newNotches;
    }

    /**
     * Returns true iff I am positioned to allow the rotor to my left
     * to advance.
     */
    boolean atNotch() {
        return _newNotches.indexOf(
                _permutation.alphabet().toChar(_position)) != -1;
    }

    /**
     * Advance me one position, if possible. By default, does nothing.
     */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /**
     * My name.
     */
    protected final String _name;

    /**
     * The permutation implemented by this rotor in its 0 position.
     */
    protected Permutation _permutation;

    /** The setting of position of the rotor. */
    protected int _position;

    /** The set of notches of the rotor. */
    protected String _notches;

    /** The set of notches turned by ring settings. */
    protected String _newNotches;
}

