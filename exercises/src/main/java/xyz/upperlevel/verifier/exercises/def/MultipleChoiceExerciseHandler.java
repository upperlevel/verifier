package xyz.upperlevel.verifier.exercises.def;

import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptySet;

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
        shuffleSelect(choices, exe.answers, exe.limit, random);
        byte[] choices_raw = ByteConvUtils.writeStringArray(choices);

        //System.out.println("encode seed end:" + getSeed(random));

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

    private <T> void shuffleSelect(List<T> in, Set<Integer> req, int limit, Random random) {
        if(req.size() > in.size())
            throw new IllegalArgumentException("The required fields are more than the actual fields! (req.length > in.length)");
        if(limit > in.size())
            throw new IllegalArgumentException("There aren't enough objects in the initial list to fulfill the limit! (limit > in.length)");
        if(limit < req.size())
            throw new IllegalArgumentException("Cannot fit all required in a list smaller than their number (req.length > limit)");

        if(req.size() == in.size() || in.size() == limit) {
            Collections.shuffle(in, random);
            return;
        }

        int index = 0;
        for(int i : req) //Put the required ints before the others
            Collections.swap(in, index++, i);

        if(limit - index > 0)
            Collections.shuffle(in.subList(index, in.size()), random);

        in.subList(limit, in.size()).clear();

        Collections.shuffle(in, random);
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

        res.put("answers", new ArrayList<>(exe.answers));

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
        shuffleSelect(mapping, req.answers, req.limit, random);

        //System.out.println("encode seed end:" + getSeed(random));

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

        List<Integer> ans = (List<Integer>) yaml.get("answers");
        exe.answers = ans == null ? emptySet() : new HashSet<>(ans);

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
