package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    //这里面放的是所有存储过的blob文件，以及历史的所有commit
    public static final File OBJECT_DIR=join(GITLET_DIR,"object");
    public static final File REFS_DIR=join(GITLET_DIR,"refs");
    public static final File heads_DIR=join(REFS_DIR,"heads");
    public static final File HEAD_FILE=join(GITLET_DIR,"HEAD");
    //stage相当于索引区域，里面代表的信息会索引最新的blob列表，即代表最新的文件内容
    public static final File STAGE_DIR=join(GITLET_DIR,"stage");
    public static Commit currCommit;
    public static Stage add_stage=new Stage();
    public static Stage remove_stage=new Stage();
    private static void init_HEAD(){
        writeContents(HEAD_FILE, "master");
    }
    private static void init_heads(){
        heads_DIR.mkdir();
        File MASTER_FILE = join(heads_DIR,"master");
        writeContents(MASTER_FILE,currCommit.getID());
        currCommit.save();
    }
    private static void init_commit(){
        currCommit=new Commit();
        currCommit.save();
    }
    public static void init(){
        if (GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        REFS_DIR.mkdir();
        STAGE_DIR.mkdir();

        init_commit();
        init_HEAD();
        init_heads();

    }
    private static Commit getlast_commit(){
        return  readObject(join(OBJECT_DIR,get_lastcommit_id()),Commit.class);
    }
    private static Stage readStage(File stage_file) {
        if (!stage_file.exists()) {
            return new Stage();
        }
        return readObject(stage_file, Stage.class);
    }

    private static void update_stage(String filename,Blob blob){
        //得到上一次commit
        Commit last_commit= getlast_commit();
        //获取当前stage内信息，便于与之前的commit仓库对比，若当前无stage，则创建
        add_stage=readStage(join(STAGE_DIR,"add"));
        remove_stage=readStage(join(STAGE_DIR,"remove"));
        //last_commit实际上代表的是当前的仓库结构
        //判断是否是仓库里没有的文件
        if (!last_commit.getblobID_map().containsValue(blob.getID())){
            //判断是否是要添加的新的blob文件，是的话先保存
            if (add_stage.isNewBlob(blob)) {
                blob.save();
                //判断新加的blob是不是和原来的那些需要add的blob有重名，
                // 是的话把原来add——stage里那个重名的blob更新成现在这个
                // 因为他们文件名一样，内容变化了所以需要变更
                if (add_stage.isFilePathExists(blob.get_path())){
                    add_stage.delete(blob);
                }
                add_stage.add(blob);
                //更新stage索引
                add_stage.add_save();
            }
        }
        //若是需要删除的文件（在remove_stage中），则再对该文件进行add指令的话
        //就相当于取消删除这个操作
        if(!remove_stage.isNewBlob(blob)){
            remove_stage.delete(blob);
            remove_stage.remove_save();
        }
        //若是之前if条件一个也没进去，相当于是新加了个stage，
        // 也就是说是第一次执行add指令，这个函数的作用将仅仅是创建stage
    }
    private static File getFileFromCWD(String file) {
        return Paths.get(file).isAbsolute()
                ? new File(file)
                : join(CWD, file);
    }
    public static void add(String filename){
        File blob= getFileFromCWD(filename);
        if (!blob.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        //将add的文件以blob形式存储
        Blob add_blob= new Blob(blob);
        add_blob.save();
        //保存或更新stage索引
        update_stage(filename,add_blob);
    }
    //得到上一个commit的id
    private static String get_lastcommit_id(){
        String branch= readContentsAsString(HEAD_FILE);
        File path= join(heads_DIR,branch);
        return readContentsAsString(path);
    }
    private static void saveHeads(Commit newCommit) {
        currCommit = newCommit;
        currCommit.save();
        String current_branch= readContentsAsString(HEAD_FILE);
        File head_point= join(heads_DIR,current_branch);
        String NewCommitID=newCommit.getID();
        writeContents(head_point,NewCommitID);
    }
    private static Map<String,String> copy_blobmap(Commit last_commit){
        Map<String,String>blob_map= new HashMap<>();
        for (Blob i:last_commit.blob_list()){
            blob_map.put(i.get_path(),i.getID());
        }
        return blob_map;
    }
    private static Map<String,String> copy_blobmap(Stage last_commit){
        Map<String,String>blob_map= new HashMap<>();
        for (Blob i:last_commit.blob_list()){
            blob_map.put(i.get_path(),i.getID());
        }
        return blob_map;
    }
    private static Map<String,String> merge_stage2blobmap(Map<String,String> blob_map){
        //获取当前stage内信息
        add_stage=readStage(join(STAGE_DIR,"add"));
        remove_stage=readStage(join(STAGE_DIR,"remove"));

        //将stage内的信息复制出来
        Map<String,String>add_blob_map= copy_blobmap(add_stage);
        Map<String,String>remove_blob_map= copy_blobmap(remove_stage);
        if (add_blob_map.isEmpty() && remove_blob_map.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        //合并到blob——map
        if (!add_blob_map.isEmpty()){
            for (String i : add_blob_map.keySet()) {
                blob_map.put(i,add_blob_map.get(i));
            }
        }
        if (!remove_blob_map.isEmpty()){
            for (String i : remove_blob_map.keySet()) {
                blob_map.put(i,remove_blob_map.get(i));
            }
        }
        return blob_map;
    }
    private static Commit update_commit_tree(String message){
        //得到上一次commit
        Commit last_commit= getlast_commit();
        //将上一个commit指向的blob信息复制到新的一个blob——map上
        Map<String,String> blob_map=copy_blobmap(last_commit);
        //根据add_stage、remove_stage的信息更新blob——map
        blob_map= merge_stage2blobmap(blob_map);
        //将blob——map信息更新到newcommit上
        List<String> parents= new ArrayList<>();
        parents.add(get_lastcommit_id()) ;
        return new Commit(message,blob_map,parents);
    }
    public static void commit(String message){
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit= new Commit();
        //commit内的tree根据stage处的blob树进行更新
        newCommit=update_commit_tree(message);
        //更新head，使其指向新的commit
        saveHeads(newCommit);
    }
    public static void checkout(){

    }

    public static void log(){

    }

    /* TODO: fill in the rest of this class. */
}
