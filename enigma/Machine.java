package enigma;

import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/**
 * Class that represents a complete enigma machine.
 *
 * @author Bradley Tian
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) throws EnigmaException {
        _alphabet = alpha;
        if (numRotors <= 1) {
            throw error("invalid number of rotors.");
        } else {
            _combination = new Rotor[numRotors];
        }
        if (pawls < 0 || pawls > numRotors) {
            throw error("invalid number of pawls.");
        } else {
            _numPawls = pawls;
        }
        if (allRotors.isEmpty()) {
            throw error("rotor list is empty.");
        } else {
            _rotors = new HashMap<>();
            for (Rotor current : allRotors) {
                _rotors.put(current.name(), current);
            }
        }
        _plugboard = new FixedRotor("Plugboard",
                new Permutation("", _alphabet));
    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _combination.length;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _numPawls;
    }

    /**
     * Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     * #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     * undefined results.
     */
    Rotor getRotor(int k) throws EnigmaException {
        if (_combination == null) {
            throw error("rotor combination has not been initialized.");
        } else {
            return _combination[k];
        }
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) throws EnigmaException {
        if (rotors.length != _combination.length) {
            throw error("Invalid rotor inputs.");
        } else {
            int pawlCheck = 0;
            for (int i = 0; i < rotors.length; i++) {

                if (!_rotors.containsKey(rotors[i])) {
                    throw error("Bad rotor name.");
                }
                _combination[i] = _rotors.get(rotors[i]);
                _combination[i].reset();

                if (_combination[i].reflecting() && i != 0) {
                    throw error("Reflector is in wrong place.");
                }

                for (Rotor r : _combination) {
                    if (r != null
                            && r.name().equals(_combination[i])) {
                        throw error("Duplicate rotor name.");
                    }
                }
                if (_combination[i].rotates()) {
                    pawlCheck++;
                }
            }
            if (pawlCheck != _numPawls) {
                throw error("Incorrect number of moving rotors.");
            }
        }

    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector).
     */
    void setRotors(String setting) throws EnigmaException {
        if (setting.length() < numRotors() - 1) {
            throw error("Wheel settings too short.");
        } else if (setting.length() > numRotors() - 1) {
            throw error("Wheel settings too long.");
        } else {
            for (int i = 0; i < setting.length(); i++) {
                if (!_alphabet.contains(setting.charAt(i))) {
                    throw error("Alphabet does not contain "
                            + "setting element.");
                } else {
                    _combination[i + 1].set(setting.charAt(i));
                }
            }
        }
    }

    /**
     * Return the current plugboard's permutation.
     */
    Permutation plugboard() {
        return _plugboard.permutation();
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard.setPermutation(plugboard);
    }

    void setRings(String rings) {
        if (rings.length() != numRotors() - 1) {
            throw error("Invalid ring setting.");
        } else {
            for (int i = 0; i < rings.length(); i++) {
                if (!_alphabet.contains(rings.charAt(i))) {
                    throw error("Invalid ring input.");
                } else {
                    _combination[i + 1].setRing(
                            _alphabet.toInt(rings.charAt(i)));
                }
            }
        }
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /**
     * Advance all rotors to their next position.
     */
    private void advanceRotors() {
        Rotor current;
        Rotor last = null;
        boolean hasLastAdvanced = false;
        boolean isLastAtNotch = false;
        for (int i = _combination.length - 1; i >= 0; i--) {
            current = _combination[i];

            if (i == _combination.length - 1) {
                if (current.rotates()) {
                    isLastAtNotch = current.atNotch();
                    current.advance();
                    last = current;
                    hasLastAdvanced = true;
                }
            } else {
                if (isLastAtNotch && current.rotates()) {
                    isLastAtNotch = current.atNotch();
                    current.advance();
                    if (!hasLastAdvanced) {
                        last.advance();
                        hasLastAdvanced = true;
                    }
                } else {
                    isLastAtNotch = current.atNotch();
                    hasLastAdvanced = false;
                }
                last = current;
            }
        }
    }

    /**
     * Return the result of applying the rotors to the character C (as an
     * index in the range 0..alphabet size - 1).
     */
    private int applyRotors(int c) {
        if (!_alphabet.contains(_alphabet.toChar(c))) {
            throw error("Improper input.");
        } else {
            for (int i = _combination.length - 1; i >= 0; i--) {
                c = _combination[i].convertForward(c);
            }
            for (int i = 1; i < _combination.length; i++) {
                c = _combination[i].convertBackward(c);
            }
        }
        return c;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) throws EnigmaException {
        String result = "";
        if (msg == null) {
            throw error("Invalid input.");
        } else {
            for (int i = 0; i < msg.length(); i++) {
                result += _alphabet.toChar(convert(
                        _alphabet.toInt(msg.charAt(i))));
            }
        }
        return result;
    }
    /** The alphabet used in this machine. */
    private final Alphabet _alphabet;

    /** The number of pawls in this machine. */
    private int _numPawls;

    /** The collection of available rotors. */
    private HashMap<String, Rotor> _rotors;

    /** The combination of rotors currently used. */
    private Rotor[] _combination;

    /** The plugboard representation of this machine. */
    private FixedRotor _plugboard;
}
