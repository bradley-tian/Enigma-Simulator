package enigma;

import static enigma.EnigmaException.*;

/**
 * Class that represents a rotating rotor in the enigma machine.
 *
 * @author Bradley Tian
 */
class MovingRotor extends Rotor {

    /**
     * A rotor named NAME whose permutation in its default setting is
     * PERM, and whose notches are at the positions indicated in NOTCHES.
     * The Rotor is initally in its 0 setting (first character of its
     * alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        for (int i = 0; i < notches.length(); i++) {
            if (!perm.alphabet().contains(notches.charAt(i))) {
                throw error("Notch position not in alphabet.");
            }
        }
        _position = 0;
        _notches = notches;
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
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        _position = super.permutation().wrap(_position + 1);
    }
}
