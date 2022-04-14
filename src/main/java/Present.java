public class Present {
    int tag;
    public Present(int tag){
        this.tag = tag;
    }

    @Override
    public int hashCode() {
        return tag;
    }

    @Override
    public String toString(){
        return String.valueOf(tag);
    }
}
