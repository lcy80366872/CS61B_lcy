package gitlet;
import static gitlet.Repository.*;
import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;
public class Blob implements Serializable {
    private String id;
    private File file_name;
    private String path;
    //文件内容
    private byte[] context;

    private String general_id() {
        return sha1(path, context);
    }
    public Blob(File file_name){
        this.file_name=file_name;
        this.path = file_name.getPath();
        this.context= readContents(file_name);
        this.id=general_id();
    }
    public String get_path(){
        return this.path;
    }
    public static Blob  getblob_byID(String id){
        File Blob_file=  join(OBJECT_DIR,id);
        return readObject(Blob_file,Blob.class);
    }
    public String getID(){
        return id;
    }
    public void save(){
        File blob_path= join(OBJECT_DIR,id);
        writeObject(blob_path,this);
    }
}
