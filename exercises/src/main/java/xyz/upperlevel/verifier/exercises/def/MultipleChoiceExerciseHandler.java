package xyz.upperlevel.verifier.exercises.def;

import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class MultipleChoiceExerciseHandler extends ExerciseType<MultipleChoiceExercise> {
    public static final MultipleChoiceExerciseHandler INSTANCE = new MultipleChoiceExerciseHandler();


    public MultipleChoiceExerciseHandler() {
        super("multi-choice");
    }

    @Override
    public ExerciseData encodeRequest(MultipleChoiceExercise exe) {
        byte[] question_raw =  exe.question.getBytes(ByteConvUtils.DEF_CHARSET);
        byte[] choices_raw = ByteConvUtils.writeStringArray(exe.choices);

        return new ExerciseData(
                exe.getType().type,
                ByteBuffer.allocate(1 + question_raw.length + 1 + choices_raw.length)
                        .put((byte) (exe.multiple ? 1 : 0))
                        .put(question_raw)
                        .put(ByteConvUtils.DEF_SEPARATOR)
                        .put(choices_raw)
                        .array()
        );
    }

    @Override
    public ExerciseData encodeResponse(MultipleChoiceExercise exe) {
        BitSet bits = new BitSet(exe.answers.size());

        exe.answers.forEach(bits::set);

        return new ExerciseData(
                exe.getType().type,
                bits.toByteArray()
        );
    }

    @Override
    public Map<String, Object> toYamlRequest(MultipleChoiceExercise exe) {
        Map<String, Object> res = new HashMap<>(2);

        res.put("multiple", exe.multiple);
        res.put("question", exe.question);
        res.put("choices", exe.choices);

        return res;
    }

    @Override
    public Map<String, Object> toYamlResponse(MultipleChoiceExercise exe) {
        return Collections.singletonMap("answers", new ArrayList<>(exe.answers));
    }



    @Override
    public MultipleChoiceExercise decodeRequest(byte[] encoded) throws IllegalFormatException {
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        MultipleChoiceExercise exe = newExe();
        exe.multiple = buffer.get() != 0;
        exe.question = ByteConvUtils.readString(buffer);
        exe.choices = Arrays.asList(ByteConvUtils.readStringArray(buffer));

        return exe;
    }

    @Override
    public MultipleChoiceExercise decodeResponse(byte[] encoded) throws IllegalFormatException {
        MultipleChoiceExercise exe = newExe();
        exe.answers =  BitSet.valueOf(encoded).stream().boxed().collect(Collectors.toSet());
        return exe;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleChoiceExercise fromYamlRequest(Map<String, Object> yaml) {
        MultipleChoiceExercise exe = newExe();
        exe.multiple = (Boolean) yaml.get("multiple");
        exe.question = (String) yaml.get("question");
        exe.choices = (List<String>)yaml.get("choices");

        return exe;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleChoiceExercise fromYamlResponse(Map<String, Object> yaml) {
        MultipleChoiceExercise exe = newExe();
        List<Integer> l = ((List<Integer>) yaml.get("answers"));
        if(l != null)
            exe.answers = new HashSet<>(l);
        return exe;
    }

    private MultipleChoiceExercise newExe() {
        return new MultipleChoiceExercise(this);
    }
}
