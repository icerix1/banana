package com.bananalab.lite;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Script {
    private static final Pattern ACTION = Pattern.compile("\\b(salt(?:a|ar|e|o|os)|gir(?:a|ar|e|o|os)|(?:desplaz|camin|mov|muev|avanz)(?:a|ar|e|er|ete|arse|ase|erse|ete)|izquierda|derecha)\\b");
    final int[] actions;
    final int[] directions;

    private Script(int[] actions, int[] directions) {
        this.actions = actions;
        this.directions = directions;
    }

    static Script parse(String input, int fallback) {
        String text = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
        if (text.isEmpty()) return new Script(new int[]{fallback}, new int[]{1});
        ArrayList<Integer> actions = new ArrayList<>();
        ArrayList<Integer> directions = new ArrayList<>();
        for (String part : text.split("[,.;!]+|\\b(?:y|luego|despues|entonces)\\b")) {
            Matcher matcher = ACTION.matcher(part);
            boolean explicit = false;
            boolean directionOnly = false;
            int direction = part.contains("izquierda") ? -1 : 1;
            while (matcher.find()) {
                String word = matcher.group();
                if (word.equals("izquierda") || word.equals("derecha")) {
                    directionOnly = true;
                    continue;
                }
                actions.add(word.startsWith("salt") ? 1 : word.startsWith("gir") ? 2 : 0);
                directions.add(direction);
                explicit = true;
            }
            if (directionOnly && !explicit) {
                actions.add(0);
                directions.add(direction);
            }
        }
        if (actions.isEmpty()) throw new IllegalArgumentException("Probá con saltar, girar o moverse a la izquierda o derecha.");
        if (actions.size() > 4) throw new IllegalArgumentException("Escribí hasta cuatro movimientos por clip.");
        int[] sequence = new int[actions.size()];
        int[] signs = new int[actions.size()];
        for (int index = 0; index < sequence.length; index++) {
            sequence[index] = actions.get(index);
            signs[index] = directions.get(index);
        }
        return new Script(sequence, signs);
    }

    float[] pose(Motion motion, float time) {
        float position = Math.max(0, Math.min(1, time)) * actions.length;
        int index = Math.min(actions.length - 1, (int) position);
        int action = actions[index];
        float[] pose = motion.predict(action, position - index);
        float[] start = motion.predict(action, 0);
        int priorTravel = 0;
        for (int step = 0; step < index; step++) {
            if (actions[step] == 0) priorTravel += directions[step];
        }
        return new float[]{
            Math.max(-0.8f, Math.min(0.8f, priorTravel * .8f + directions[index] * (pose[0] - start[0]))),
            pose[1] - start[1],
            directions[index] * (pose[2] - start[2])
        };
    }
}
