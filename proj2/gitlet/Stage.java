package gitlet;
import static gitlet.Repository.STAGE_DIR;
import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//stage内部这个tree结构和commit里面的那个树结构功能是一致的
public class Stage implements Serializable {
    //tree代表的是需要commit的（filename到blob值）这个map
    private Map<String,String> tree = new HashMap<>();
    public Stage(String file_name,String blob_id){
        tree.put(file_name,blob_id);
    }
    public Stage (){
        tree = new HashMap<>();
    }

    public boolean isFilePathExists(String path) {
        return tree.containsKey(path);
    }
    public boolean isNewBlob(Blob blob) {
        return !tree.containsValue((blob.getID()));
    }
    public void add(Blob blob) {
        tree.put(blob.get_path(), blob.getID());
    }
    public void delete(Blob blob) {
        tree.remove(blob.get_path());
    }
    public void add_save(){
        File add_stage = join(STAGE_DIR,"add");
        writeObject(add_stage,this);
    }
    public void remove_save(){
        File remove_stage = join(STAGE_DIR,"remove");
        writeObject(remove_stage,this);
    }
    public List<String> blobid_list(){
        List<String> list =new ArrayList<>(tree.values());
        System.out.println(tree.values());
        return list;
    }
    public List<Blob> blob_list(){
        Blob blob;
        List<Blob> list=new ArrayList<>();
        for (String i : tree.values()){
            blob= Blob.getblob_byID(i);
            list.add(blob);
        }
        return list;
    }

}
