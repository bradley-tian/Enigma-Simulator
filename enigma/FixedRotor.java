package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotor that has no ratchet and does not advance.
 *  @author Bradley Tian
 */
class FixedRotor extends Rotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is given by PERM. */
    FixedRotor(String name, Permutation perm) {
        super(name, perm);
        _position = 0;
    }

    @Override
    void set(int posn) throws EnigmaException {
        if (posn < 0 || posn >= super.permutation().alphabet().size()) {
            throw error("Position not in alphabet.");
        } else {
            _position = posn;
        }
    }

    @Override
    int convertForward(int p) {
        int dest = super.convertForward(p + _position);
        return super.permutation().wrap(dest);
    }

    @Override
    int convertBackward(int e) {
        int dest = super.convertBackward(e + _position);
        return super.permutation().wrap(dest);
    }

    @Override
    boolean atNotch() {
        return false;
    }

    /** The setting position of this rotor. */
    protected int _position;
}
