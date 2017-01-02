package xyz.upperlevel.verifier.exercises.def;

import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultipleChoiceExerciseHandler extends ExerciseType<MultipleChoiceExerciseRequest, MultipleChoiceExerciseResponse> {//TODO redo encoding/decoding
    public static final MultipleChoiceExerciseHandler INSTANCE = new MultipleChoiceExerciseHandler();


    public MultipleChoiceExerciseHandler() {
        super("multi-choice");
    }

    @Override
    public ExerciseData encodeRequest(MultipleChoiceExerciseRequest exe,  Random random) {
        //System.out.println("encode seed:" + getSeed(random));
        byte[] question_raw =  exe.question.getBytes(ByteConvUtils.DEF_CHARSET);

        List<String> choices = new ArrayList<>(exe.choices);
        Collections.shuffle(choices, random);
        if(choices.size() > exe.limit)
            choices = choices.subList(0, exe.limit);
        byte[] choices_raw = ByteConvUtils.writeStringArray(choices);

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
    public ExerciseData encodeResponse(MultipleChoiceExerciseResponse exe) {
        BitSet bits = new BitSet(exe.answers.size());

        exe.answers.forEach(bits::set);

        return new ExerciseData(
                exe.getType().type,
                bits.toByteArray()
        );
    }

    @Override
    public Map<String, Object> toYamlRequest(MultipleChoiceExerciseRequest exe) {
        Map<String, Object> res = new HashMap<>(2);

        res.put("multiple", exe.multiple);
        res.put("question", exe.question);


        res.put("choices", exe.choices);
        res.put("limit", exe.limit);

        return res;
    }

    @Override
    public Map<String, Object> toYamlResponse(MultipleChoiceExerciseResponse exe) {
        return Collections.singletonMap("answers", new ArrayList<>(exe.answers));
    }



    @Override
    public MultipleChoiceExerciseRequest decodeRequest(byte[] encoded) throws IllegalFormatException {
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        MultipleChoiceExerciseRequest exe = newReq();
        exe.multiple = buffer.get() != 0;
        exe.question = ByteConvUtils.readString(buffer);
        exe.choices = Arrays.asList(ByteConvUtils.readStringArray(buffer));

        return exe;
    }

    @Override
    //TODO: reimplement shuffle for ints
    public MultipleChoiceExerciseResponse decodeResponse(byte[] encoded, MultipleChoiceExerciseRequest req, Random random) throws IllegalFormatException {
        //System.out.println("decode seed:" + getSeed(random));
        MultipleChoiceExerciseResponse exe = newRes(req);

        List<Integer> mapping = IntStream.range(0, req.choices.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(mapping, random);

        //System.out.println("mapping: " + mapping);

        exe.answers = BitSet.valueOf(encoded)
                .stream()
                .filter(i -> i >= 0 && i < req.limit)
                .map(mapping::get)//de-map: they were previously mapped so this should return their original value
                .boxed().collect(Collectors.toSet());
        return exe;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleChoiceExerciseRequest fromYamlRequest(Map<String, Object> yaml) {
        MultipleChoiceExerciseRequest exe = newReq();
        exe.multiple = (Boolean) yaml.get("multiple");
        exe.question = (String) yaml.get("question");
        exe.choices = (List<String>)yaml.get("choices");
        exe.limit = (Integer)yaml.getOrDefault("limit", exe.choices.size());

        return exe;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MultipleChoiceExerciseResponse fromYamlResponse(Map<String, Object> yaml, MultipleChoiceExerciseRequest req) {
        MultipleChoiceExerciseResponse exe = newRes(req);
        List<Integer> l = ((List<Integer>) yaml.get("answers"));
        if(l != null)
            exe.answers = new HashSet<>(l);
        return exe;
    }

    private MultipleChoiceExerciseRequest newReq() {
        return new MultipleChoiceExerciseRequest(this);
    }

    private MultipleChoiceExerciseResponse newRes(MultipleChoiceExerciseRequest req) {
        return new MultipleChoiceExerciseResponse(this, req);
    }

    /*private long getSeed(Random random) {
        try {
            Field field = Random.class.getDeclaredField("seed");
            field.setAccessible(true);
            return ((AtomicLong) field.get(random)).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/
}
