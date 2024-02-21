package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

import gitlet.Utils.*;

import javax.swing.plaf.PanelUI;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private List<String> parents;
    //  path->blobid 的映射
    private Map<String,String> blobID=new HashMap<>();
    private Date currentTime;
    private String id;
    private String timeStamp;
    private  File filepath;

    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }
    private String general_id(){
        return Utils.sha1(timeStamp,message,parents.toString(),blobID.toString());
    }
    public Commit(String message,Map<String,String> blobID,List<String> parents){
        this.message=message;
        this.blobID=blobID;
        this.parents=parents;
        this.currentTime=new Date(0);
        this.timeStamp=dateToTimeStamp(this.currentTime);
        this.id=general_id();
        this.filepath=join(OBJECT_DIR,id);
    }
    public Commit(){
        this.message="initial commit";
        this.blobID=new HashMap<>();
        this.parents=new ArrayList<>();
        this.currentTime=new Date(0);
        this.timeStamp=dateToTimeStamp(this.currentTime);
        this.id=general_id();
        this.filepath=join(OBJECT_DIR,id);
    }
    public String getID(){
        return this.id;
    }
    public void add(Blob blob){
        blobID.put(blob.get_path(),blob.getID());
    }
    public Map<String,String> getblobID_map(){
        return this.blobID;
    }
    public void save(){
        writeObject(filepath,this);
    }
    public List<String> blobid_list(){
        List<String> list =new ArrayList<>(blobID.values());
        return list;
    }
    public List<Blob> blob_list(){
        Blob blob;
        List<Blob> list=new ArrayList<>();
        for (String i : blobID.values()){
            blob= Blob.getblob_byID(i);
            list.add(blob);
        }
        return list;
    }

    /* TODO: fill in the rest of this class. */
}
