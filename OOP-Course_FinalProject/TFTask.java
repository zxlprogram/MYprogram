public class TFTask extends Task {
    public TFTask(MoodleServer server,String t, String context, boolean correctAnswer) {
        super(server,t, context,correctAnswer);
    }
    @Override
    public void requestDoTask(MoodleUser mdu,Object o) {
        if(!(o instanceof Boolean)) {
            throw new IllegalArgumentException("TFTask requires Boolean answer");
        }
        getServer().submitAnswer(mdu,this,(boolean)o);
    }
    @Override
    public boolean checkAnswer(MoodleUser mdu) {
        return getCorrectAnswer().equals(getUserAnswer().get(mdu));
    }
    @Override
    public String result(MoodleUser mdu) {
        return Boolean.toString(checkAnswer(mdu));
    }
    @Override
    public void requestAddTask(MoodleUser mdu) {
        if(mdu.getPermissions().contains(PERMISSION.EDIT_TASK)) {
            getServer().addTask(this,mdu);
        }
    }
}