package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a reflector in the enigma.
 *  @author Bradley Tian
 */
class Reflector extends FixedRotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is PERM. */
    Reflector(String name, Permutation perm) {
        super(name, perm);
        _position = 0;
    }

    @Override
    boolean reflecting() {
        return true;
    }

    @Override
    void set(int posn) throws EnigmaException {
        if (posn != 0) {
            throw error("reflector has only one position");
        }
    }
}
