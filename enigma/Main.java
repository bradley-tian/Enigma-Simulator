package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/**
 * Enigma simulator.
 *
 * @author Bradley Tian
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        Machine machine = readConfig();
        while (_input.hasNext()) {
            if (_reserve.isEmpty()) {
                setUpRotors(machine);
            } else {
                setUpRotors(machine, _reserve);
            }
            ArrayList<String> input = compileInputMessage();
            ArrayList<String> output = compileOutputMessage(machine, input);
            printMessageLine(output);
        }
        try {
            String end = _input.nextLine();
            if (end.equals("")) {
                _output.println(end);
            }
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            String alpha = _config.nextLine();
            alpha.trim();
            _alphabet = new Alphabet(alpha);
            _rotors = new ArrayList<>();

            int numRotors = -1;
            int numPawls = -1;
            Scanner numerics = new Scanner(_config.nextLine());
            while (numerics.hasNext()) {
                if (numerics.hasNextInt()) {
                    if (numRotors == -1) {
                        numRotors = numerics.nextInt();
                    } else if (numPawls == -1) {
                        numPawls = numerics.nextInt();
                    } else {
                        throw error("excess numerics in config.");
                    }
                } else {
                    numerics.next();
                }
            }

            while (_config.hasNext()) {
                _config.useDelimiter("[\\s]+");
                String rotorName = _config.next();
                String typeNotch = _config.next();
                char rotorType = typeNotch.charAt(0);
                String notches = typeNotch.substring(1);
                String cycles = parseCycles(_config);

                Permutation newPerm = new Permutation(cycles, _alphabet);
                if (rotorType == 'R') {
                    Reflector newReflector = new Reflector(
                            rotorName, newPerm);
                    _rotors.add(newReflector);
                } else if (rotorType == 'N') {
                    FixedRotor newFixed = new FixedRotor(
                            rotorName, newPerm);
                    _rotors.add(newFixed);
                } else if (rotorType == 'M') {
                    MovingRotor newMoving = new MovingRotor(
                            rotorName, newPerm, notches);
                    _rotors.add(newMoving);
                }
            }

            return new Machine(_alphabet, numRotors, numPawls, _rotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    private String parseCycles(Scanner sc) {
        String cycles = "";
        sc.useDelimiter("[\\s]+");
        while (sc.hasNext("\\(.*\\)")) {
            Pattern cyclePattern = Pattern.compile("(\\(.*\\))+");
            Matcher cycleMatcher = cyclePattern.matcher(sc.nextLine());
            while (cycleMatcher.find()) {
                String newCycle = cycleMatcher.group();
                if (newCycle.length() <= 2) {
                    throw error("Empty cycle in config.");
                }
                for (int i = 0; i < newCycle.length(); i++) {
                    if (newCycle.charAt(i) != '('
                            && newCycle.charAt(i) != ')'
                            && newCycle.charAt(i) != ' '
                            && !_alphabet.contains(newCycle.charAt(i))) {
                        throw error("Invalid cycle config.");
                    }
                }
                cycles += newCycle + " ";
            }
        }
        return cycles;
    }

    /**
     * Set MACHINE according to the specification given by input,
     * which must have the format specified in the assignment.
     */
    private void setUpRotors(Machine machine) {
        _input.useDelimiter("[\\s]+");
        if (_input.hasNext("\\*")) {
            String settings = _input.nextLine();
            Scanner sc = new Scanner(settings);
            sc.useDelimiter("[\\s]+");
            sc.next();
            String[] combination = new String[machine.numRotors()];
            for (int i = 0; i < machine.numRotors(); i++) {
                combination[i] = sc.next();
            }
            machine.insertRotors(combination);

            String setting = sc.next();
            String rings = "";
            machine.setRotors(setting);

            if (sc.hasNext("[\\w]+")) {
                rings = sc.next();
            }

            if (sc.hasNext("(\\(.*\\))+")) {
                String plugboard = parseCycles(sc);
                machine.setPlugboard(new Permutation(plugboard, _alphabet));
            }

            if (!rings.equals("")) {
                machine.setRings(rings);
            }

        } else {
            throw error("Malformed start of input setting.");
        }
    }

    private void setUpRotors(Machine machine, String reserve) {
        Scanner sc = new Scanner(reserve);
        sc.useDelimiter("[\\s]+");
        if (sc.hasNext("\\*")) {
            sc.next();
            String[] combination = new String[machine.numRotors()];
            for (int i = 0; i < machine.numRotors(); i++) {
                combination[i] = sc.next();
            }

            String setting = sc.next();
            String rings = "";

            if (sc.hasNext("[\\w]+")) {
                rings = sc.next();
            }

            if (sc.hasNext("(\\(.*\\))+")) {
                String plugboard = parseCycles(sc);
                machine.setPlugboard(new Permutation(plugboard, _alphabet));
            }

            machine.insertRotors(combination);
            machine.setRotors(setting);

            if (!rings.equals("")) {
                machine.setRings(rings);
            }

        } else {
            throw error("Malformed start of input setting.");
        }
    }

    private ArrayList<String> compileInputMessage() {
        ArrayList<String> input = new ArrayList<>();
        while (_input.hasNextLine()
                && !_input.hasNext("\\*")) {
            String line = _input.nextLine();
            String condensed = "";
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) != '('
                        && line.charAt(i) != ')'
                        && line.charAt(i) != ' '
                        && !_alphabet.contains(line.charAt(i))) {
                    throw error("input elements "
                            + "not contained within alphabet.");
                } else if (line.charAt(i) != ' ') {
                    condensed += Character.toString(line.charAt(i));
                }
            }
            input.add(condensed);
        }
        if (_input.hasNextLine()) {
            String test = _input.nextLine();
            if (test.equals("")) {
                input.add(test);
            } else {
                _reserve = test;
            }
        }
        return input;
    }

    private ArrayList<String> compileOutputMessage(Machine machine,
                                                   ArrayList<String> input) {
        ArrayList<String> output = new ArrayList<>();
        for (String s : input) {
            if (s.equals("")) {
                output.add(s);
            } else {
                output.add(machine.convert(s));
            }
        }
        return output;
    }

    /**
     * Return true iff verbose option specified.
     */
    static boolean verbose() {
        return _verbose;
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     * @param output the output to be printed.
     */

    private void printMessageLine(ArrayList<String> output) {
        for (String s : output) {
            String formatted = "";
            if (s.equals("")) {
                _output.println(s);
            } else {
                for (int i = 0; i < s.length(); i++) {
                    if ((i + 1) % 5 == 0) {
                        formatted += Character.toString(s.charAt(i)) + " ";
                    } else {
                        formatted += Character.toString(s.charAt(i));
                    }
                }
                _output.println(formatted);
            }
        }
    }

    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;

    /**
     * True if --verbose specified.
     */
    private static boolean _verbose;

    /**
     * Set of available rotors used in this machine.
     */
    private ArrayList<Rotor> _rotors;

    /**
     * List of forbidden alphabet characters.
     */
    private char[] forbidden = {'(', ')', ' '};

    /**
     * Subsequent setting line if parsed.
     */
    private String _reserve = "";
}
