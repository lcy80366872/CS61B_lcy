package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

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
        String id =get_lastcommit_id();
        File commit_file = join(OBJECT_DIR,id);
        return  readObject(commit_file,Commit.class);
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
                blob_map.remove(i,remove_blob_map.get(i));
            }
        }
        add_stage.clear();
        remove_stage.clear();
        add_stage.add_save();
        remove_stage.remove_save();
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
    public static boolean deleteFile(File file) {
        if (!file.isDirectory()) {
            if (file.exists()) {
                return file.delete();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public static Commit getCommitByID(String id){
        File commit_file = join(OBJECT_DIR,id);
        return readObject(commit_file,Commit.class);
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
    public static void rm(String file_name){
        add_stage=readStage(join(STAGE_DIR,"add"));
        remove_stage=readStage(join(STAGE_DIR,"remove"));
        Commit currcommit= getlast_commit();
        //file_name 需要先转化成绝对路径，便于后续操作
        String file_path = join(CWD,file_name).getPath();
        //判断该文件是否在add——stage中，有的话删除
        if (add_stage.isFilePathExists(file_path)){
            add_stage.delete(file_path);
            add_stage.add_save();
        }
        //若该文件被当前指向commit跟踪，（即当前commit_tree里有这个文件）将它放在remove——stage并且删除工作区该文件
        //若文件不存在于工作目录中，不删除即可
        //放到remove缓冲区后，下一次commit_tree就不会有该文件了
        else if (currcommit.exist_filepath(file_path)){
            remove_stage.add(currcommit.getblob_byfilepath(file_path));
            remove_stage.remove_save();
            File file= join(CWD,file_name);
            deleteFile(file);
        }
        //若都不满足，报错
        else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }
    public static void clear_stage(){
        add_stage=readStage(join(STAGE_DIR,"add"));
        remove_stage=readStage(join(STAGE_DIR,"remove"));
        add_stage.clear();
        remove_stage.clear();
        add_stage.add_save();
        remove_stage.remove_save();
    }
    public static void print_information(Commit i){
        System.out.println("===");
        System.out.println("commit "+ i.getID());
        System.out.println("Date: "+i.getTime());
        System.out.println(i.getMessage());
        System.out.println();
    }
    public static void print_merge_information(Commit i){
        System.out.println("===");
        System.out.println("commit "+ i.getID());
        String parent1= i.getParents().get(0);
        String parent2= i.getParents().get(1);
        System.out.println("Merge: "+parent1.substring(0, 7) + " " + parent2.substring(0, 7));
        System.out.println("Date: "+i.getTime());
        System.out.println(i.getMessage());
        System.out.println();
    }
    public static void log(){
        Commit commit = getlast_commit();
        if (!commit.getParents().isEmpty()){
            for(Commit i=getlast_commit();!i.getParents().isEmpty();i=getCommitByID(i.getParents().get(0))){

                if (i.getParents().size()<=1){
                    print_information(i);
                }
                else {
                    print_merge_information(i);
                }
                //这个是为了保留信息，这样退出循环后，可以把最初创建的commit信息打印出来
                commit=i;
            }
            commit=getCommitByID(commit.getParents().get(0));
            print_information(commit);
        }
        else {
            print_information(commit);
        }
        System.exit(0);
    }
    public static void global_log(){
        List<String> obj_list=plainFilenamesIn(OBJECT_DIR);
        for (String s : obj_list) {
            try {
                File obj_file = join(OBJECT_DIR, s);
                Commit commit = readObject(obj_file, Commit.class);
                if (commit.getParents().size() <= 1) {
                    print_information(commit);
                } else {
                    print_merge_information(commit);
                }
            } catch (Exception ignore) {
            }
        }
    }
    public static void find(String message){
        List<String> obj_list=plainFilenamesIn(OBJECT_DIR);
        List<Commit> commit_list = new ArrayList<>();
        for (String s : obj_list) {
            try {
                File obj_file = join(OBJECT_DIR, s);
                Commit commit = readObject(obj_file, Commit.class);
                if (commit.getMessage().equals(message)) {
                    commit_list.add(commit);
                }
            } catch (Exception ignore) {
            }
        }
        if (commit_list.isEmpty()){
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        else {
            for (Commit i :commit_list){
                System.out.println(i.getID());
            }
        }
    }
    public static void status(){
        System.out.println("=== Branches ===");
        List<String> branches_list=plainFilenamesIn(heads_DIR);
        String current_branch=readContentsAsString(HEAD_FILE);
        for (String i : branches_list){
            if (i.equals(current_branch)){
                System.out.println("*"+i);
            }else System.out.println(i);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        add_stage=readStage(join(STAGE_DIR,"add"));

        for (Blob i: add_stage.blob_list()){
            System.out.println(i.get_filename());
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        remove_stage=readStage(join(STAGE_DIR,"remove"));
        for (Blob i: remove_stage.blob_list()){
            System.out.println(i.get_filename());
        }
        System.out.println();

        //待定
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");

        System.out.println();
    }
    private static void overwrite_file(String file_name,Commit commit){
        File file= join(CWD,file_name);
        String file_path=file.getPath();
        if (commit.exist_filepath(file_path)){
            Blob file_blob = commit.getblob_byfilepath(file_path);
            byte[] context = file_blob.getContext();
            writeContents(file,new String(context, StandardCharsets.UTF_8));
        }
    }
    //checkout第一种情况
    public static void checkout(String file_name){
        Commit last_commit=getlast_commit();
        File file= join(CWD,file_name);
        String file_path=file.getPath();
        //若Commit追踪的文件包含filename
        //则将其写入到工作目录：如果同名文件存在，overwrite它
        // 如果不存在，直接写入。
        if (last_commit.exist_filepath(file_path)){
            Blob file_blob = last_commit.getblob_byfilepath(file_path);
            byte[] context = file_blob.getContext();
            writeContents(file,new String(context, StandardCharsets.UTF_8));
        }
        //若当前的Commit中不存在filename的文件，
        // 输出File does not exist in that commit.
        else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }
    //checkout 第二种情况
    //与第一种情况类似，只不过现在是从之前的某个Commit追踪的文件中
    // 把filename的文件拉过来
    public static void checkout(String commitID,String file_name){
        List<String> blob_list = plainFilenamesIn(OBJECT_DIR);
        if (!blob_list.contains(commitID) ) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit=getCommitByID(commitID);
        File file= join(CWD,file_name);
        String file_path=file.getPath();
        if (commit.exist_filepath(file_path)){
            Blob file_blob = commit.getblob_byfilepath(file_path);
            byte[] context = file_blob.getContext();
            writeContents(file,new String(context, StandardCharsets.UTF_8));
        }//若当前的Commit中不存在filename的文件，
        // 输出File does not exist in that commit.
        else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }
    private static List<String> get_blob_name(List<String> BlobID_list) {
        List<String> name_list = new ArrayList<>();
        for (String i:BlobID_list){
            Blob blob = Blob.getblob_byID(i);
            name_list.add(blob.get_filename());
        }
        return name_list;
    }
    private static List<String> get_blobName(List<Blob> Blob_list) {
        List<String> name_list = new ArrayList<>();
        for (Blob i:Blob_list){
            name_list.add(i.get_filename());
        }
        return name_list;
    }

    //checkout第三种情况
    //切换Branch
    public static void checkout_branch(String branch) {
        //若要选择的branch不存在，退出
        File branch_file= join(heads_DIR,branch);
        if (!branch_file.exists()){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        //读取当前branch的commit
        String ori_branch=readContentsAsString(HEAD_FILE);
        String commit_id= readContentsAsString(join(heads_DIR,ori_branch));
        Commit ori_commit= getCommitByID(commit_id);
        //如果branch就是当前分支
        if (ori_branch.equals(branch)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        //切换Branch，并读取新commit
        writeContents(HEAD_FILE,branch);
        String id= readContentsAsString(branch_file);
        Commit new_commit= getCommitByID(id);
        //把切换后的分支所跟踪的文件移到当前目录
        List<String> oriBlobID_list=ori_commit.blobid_list();
        List<String> newBlobID_list=new_commit.blobid_list();
        for (String newID : newBlobID_list){
            Blob newblob = Blob.getblob_byID(newID);
            List<String> name_list =get_blob_name(oriBlobID_list);


            if (!oriBlobID_list.contains(newID)){
                // 该文件现在内容相比原先变化了，则overwrite，
                if (!name_list.contains(newblob.get_filename())){
                    byte[] context = newblob.getContext();
                    writeContents(newblob.get_File(), new String(context, StandardCharsets.UTF_8));
                }
                else {
                    //这里存在特殊情况
                    //将要直接写入的时候如果有同名文件已经在工作目录中了，说明工作目录中在执行checkout前增加了新的文件而没有commit
                    if(plainFilenamesIn(CWD).contains(newblob.get_filename())) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        System.exit(0);
                    }//原来没有，切换后有了的文件，且不是特殊情况的，直接写入
                    else {
                        byte[] context = newblob.getContext();
                        writeContents(newblob.get_File(), new String(context, StandardCharsets.UTF_8));
                    }
                }

            }
        }
        //对于切换后分支没有，而切换前分支中有的文件，删掉
        for (String oriID : oriBlobID_list){
            if (!newBlobID_list.contains(oriID)) {
                Blob oriblob = Blob.getblob_byID(oriID);
                File ori_file = oriblob.get_File();
                //若其在工作目录中，删掉
                if (ori_file.exists()) {
                    deleteFile(oriblob.get_File());
                }
            }
                //若不在，不管
        }
        //清空缓存区（stage）
        clear_stage();
    }
    public static void branch(String branch){
        List <String> branch_list = plainFilenamesIn(heads_DIR);
        if (branch_list.contains(branch)){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File new_branch = join(heads_DIR,branch);
        writeContents(new_branch,getlast_commit().getID());
    }
    public static void rm_branch(String branch){
        File delete_branch = join(heads_DIR,branch);
        if (delete_branch.exists()){
            String ori_branch=readContentsAsString(HEAD_FILE);
            if (branch.equals(ori_branch)){
                System.out.println("Cannot remove the current branch.");
                System.exit(0);
            }
            deleteFile(delete_branch);
        }else {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }
    private static void change_commit(Commit NewCommit){
        String curr_branch = readContentsAsString(HEAD_FILE);
        File currcommit_file = join(heads_DIR, curr_branch);
        //获得新旧commit
        String commit_id= NewCommit.getID();
        String OldCommitID= readContentsAsString(currcommit_file);
        Commit OldCommit =  getCommitByID(OldCommitID);
        //将当前指向的commit换成我们所需的commit_id的那个commit
        writeContents(currcommit_file, commit_id);
        //获得变换commit后，当前工作目录需要有哪些文件
        List<Blob> commit_file_list= NewCommit.blob_list();
        List<Blob> old_commit_file_list= OldCommit.blob_list();

        List<Blob> add_file_list = new ArrayList<>();
        //当前目录需要变化的文件（包括需要添加和重写的）
        List<Blob> change_file_list = new ArrayList<>();
        //当前目录需要删除的文件
        List<Blob> delete_file_list = new ArrayList<>();
        for (Blob i : commit_file_list){
            List<String> name_list =get_blobName(old_commit_file_list);
            if (!old_commit_file_list.contains(i)){
                //若之前的commit中没有这些文件，则将其列入待添加文件列表，同时考虑特殊情况
                if(!name_list.contains(i.get_filename())){
                    add_file_list.add(i);
                }
                else change_file_list.add(i);
            }
        }
        List<String> curr_file_list= plainFilenamesIn(CWD);
        for (String i :curr_file_list){
            //变换后目录里面应该有的文件名字
            List<String> new_file_name =get_blobName(commit_file_list);
            if (!new_file_name.contains(i)){
                File file = join(CWD,i);
                Blob file_blob = new Blob(file) ;
                delete_file_list.add(file_blob);
            }
        }
        for (Blob i :add_file_list){
            //这里存在特殊情况
            //将要直接写入的时候如果有同名文件已经在工作目录中了，说明工作目录中在执行checkout前增加了新的文件而没有commit
            if(plainFilenamesIn(CWD).contains(i.get_filename())) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
//            System.out.println("add:"+i.get_filename());
            checkout(commit_id,i.get_filename());
        }
        for (Blob i :change_file_list){
            checkout(commit_id,i.get_filename());
//            System.out.println("change:"+i.get_filename());
        }
        for (Blob i :delete_file_list){
            File delete_file = join(CWD,i.get_filename());
//            System.out.println("delete:"+delete_file.getName());
            deleteFile(delete_file);
        }
        clear_stage();
    }
    public static void reset(String commit_id){
        List<String> file_list =plainFilenamesIn(OBJECT_DIR);
        if (file_list.contains(commit_id)) {
            Commit commit= getCommitByID(commit_id);
            change_commit(commit);
        }
        else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }
    //split point是两个分支的最近距离的分开的Commit节点
    public static Commit FindSplitNode(Map<String,Integer> map1,Map<String,Integer> map2){
        String min_id = "";
        //两个分支可能有多个分裂点，设置这个是为了找到两个分支最近那个分裂点，
        // 至于这个分裂点的深度，以一个分支的值为主即可
        int min_length= Integer.MAX_VALUE ;
        for (String id: map1.keySet()){
            if(map2.containsKey(id) && map2.get(id)< min_length){
                min_id = id;
                min_length= map2.get(id);
            }
        }
        return getCommitByID(min_id);
    }
    public static Map <String,Integer> getMap_fromCommit(Commit commit,int length){

        Map<String,Integer> map= new HashMap<>();
        if(commit.getParents().isEmpty()){
            map.put(commit.getID(),length);
            return map;
        }
        //把当前commitid记录并增加一层深度，深度代表此commit距离branch头远
        map.put(commit.getID(),length);
        length ++;
        //一个commit可能有多个parents
        for (String i : commit.getParents()){
            map.putAll(getMap_fromCommit(getCommitByID(i),length));
        }
        return map;
    }
    private static List<String> add_file_list( Map<String,String> split_blobs ,Map<String,String>curr_blobs,Map<String,String> merge_blobs){
        List<String> add_blobs = new ArrayList<>();

        List<String> delete_blobs = new ArrayList<>();
        for (String i: merge_blobs.keySet()){
            //case5:若相较于split处，merge分支新加文件，而curr分支没变化，则添加新文件
            if (!split_blobs.containsKey(i)&& !curr_blobs.containsKey(i)){
                add_blobs.add(merge_blobs.get(i));
            }
        }
        return add_blobs;
    }
    private static List<String> overwrite_file_list(  Map<String,String> split_blobs ,Map<String,String>curr_blobs,Map<String,String> merge_blobs){
        List<String> overwrite_blobs = new ArrayList<>();
        for (String i : split_blobs.keySet()) {
            if (merge_blobs.containsKey(i) && curr_blobs.containsKey(i)) {
                //case1:若相较于split处，merge分支文件内容变化，而curr分支文件内容没变化，则更改内容
                if (split_blobs.get(i).equals(curr_blobs.get(i)) && !split_blobs.get(i).equals(merge_blobs.get(i))) {
                    overwrite_blobs.add(merge_blobs.get(i));
                }
            }
        }
        return overwrite_blobs;
    }

    private static List<String> delete_file_list( Map<String,String> split_blobs ,Map<String,String>curr_blobs,Map<String,String> merge_blobs){
        List<String> delete_blobs = new ArrayList<>();
        for (String i: split_blobs.keySet()){
            //case6:若相较于split处，merge分支删除了文件，而curr分支没变化，则添加要删除的文件到列表
            if (!merge_blobs.containsKey(i)&& curr_blobs.containsKey(i)){
                delete_blobs.add(curr_blobs.get(i));
            }
        }
        return delete_blobs;
    }
    private static void conflict_message(String curr_blobID,String merge_blobID){
        String currBranchContents= readContentsAsString(join(OBJECT_DIR,curr_blobID));
        String mergeBranchContents= readContentsAsString(join(OBJECT_DIR,merge_blobID));
        String conflictContents = "<<<<<<< HEAD\n" + currBranchContents + "=======\n" + mergeBranchContents + ">>>>>>>\n";
        String file_name = Blob.getblob_byID(curr_blobID).get_filename();
        File file = join(CWD,file_name);
        writeContents(file,conflictContents);
    }
    private static void deal_conflict(Map<String,String> split_blobs ,Map<String,String>curr_blobs,Map<String,String> merge_blobs){
        for (String i :split_blobs.keySet()){
            //两分支的文件内容都做了更改，但不一致
            if (merge_blobs.containsKey(i)&&curr_blobs.containsKey(i)){
                if (!merge_blobs.get(i).equals(curr_blobs.get(i))){
                    System.out.println("Encountered a merge conflict.");
                    conflict_message(curr_blobs.get(i),merge_blobs.get(i));
                }
            }
            //其中一分支删除了某文件，另一分支却又对该文件增加了内容
            if (merge_blobs.containsKey(i)&&!curr_blobs.containsKey(i)){
                if (!split_blobs.get(i).equals(merge_blobs.get(i))){
                    System.out.println("Encountered a merge conflict.");
                    conflict_message(curr_blobs.get(i),merge_blobs.get(i));
                }
            }
            if (curr_blobs.containsKey(i)&&!merge_blobs.containsKey(i)){
                if (!split_blobs.get(i).equals(curr_blobs.get(i))){
                    System.out.println("Encountered a merge conflict.");
                    conflict_message(curr_blobs.get(i),merge_blobs.get(i));
                }
            }
        }
    }
    private static Map<String,String> merge_file(List<String> AllFile, Map<String,String> split_blobs,Map<String,String>curr_blobs,Map<String,String> merge_blobs){
        //上面后三个参数是每个相关节点指向的文件map(path->blobID)
        currCommit= getlast_commit();
        //case5
        List<String> add_blobsID= add_file_list(split_blobs,curr_blobs,merge_blobs);
        //case1
        List<String> overwrite_blobsID=overwrite_file_list(split_blobs,curr_blobs,merge_blobs);
        //case6
        List<String> delete_blobsID= delete_file_list(split_blobs,curr_blobs,merge_blobs);
        //根据当前的commit的blobmap，对其进行修改，得出融合后的map
        Map<String,String>map= merge_file_map(add_blobsID,overwrite_blobsID,delete_blobsID,currCommit);

        //case 2 4 7 3-1: do nothing

        //返回map
        return map;

    }
    private static void checkIfSplitPintIsGivenBranch(Commit splitPoint, Commit mergeCommit) {
        if (splitPoint.getID().equals(mergeCommit.getID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
    }
    private static void checkIfStageEmpty() {
        add_stage=readStage(join(STAGE_DIR,"add"));
        remove_stage=readStage(join(STAGE_DIR,"remove"));
        if (!(add_stage.isEmpty() && remove_stage.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }
    private static void checkIfBranchExist(String branch){
        List<String> allBranch = plainFilenamesIn(heads_DIR);
        if (!allBranch.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }
    private static void checkIfMergeWithSelf(String branchName) {
        String currBranch = readContentsAsString(HEAD_FILE);
        if (currBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }
    private static void checkIfSplitPintIsCurrBranch(Commit splitPoint, String mergeBranch) {
        if (splitPoint.getID().equals(currCommit.getID())) {
            System.out.println("Current branch fast-forwarded.");
            checkout_branch(mergeBranch);
        }
    }
    public static void merge(String branch){

        checkIfBranchExist(branch);
        checkIfStageEmpty();
        checkIfMergeWithSelf(branch);
        //读取当前commit和要merged_branch的最后commit
        currCommit = getlast_commit();
        String merge_id = readContentsAsString(join(heads_DIR,branch));
        Commit mergeCommit= getCommitByID(merge_id) ;
        //将两个branch途径的所有commitID和对应的深度，放入两个map里，便于后续操作
        Map<String,Integer> currMap= getMap_fromCommit(currCommit,0);
        Map<String,Integer> mergeMap= getMap_fromCommit(mergeCommit,0);
        //寻找分裂节点（考虑只有一个分裂节点）
        Commit split_commit = FindSplitNode(currMap,mergeMap);
        //判断该节点的两种情况
        checkIfSplitPintIsGivenBranch(split_commit,mergeCommit);
        checkIfSplitPintIsCurrBranch(split_commit,currCommit.getID());
        //将分裂点处、currCommit处、mergeCommit处跟踪的所有文件blobID都存在AllBlob中
        List<String> AllBlob  =  new ArrayList<>(split_commit.blobid_list());
        AllBlob.addAll(currCommit.blobid_list());
        AllBlob.addAll(mergeCommit.blobid_list());
        Set<String> set  = new HashSet<>(AllBlob);//这里利用set这个类（相当于无序，并且无重复元素的列表）
        AllBlob.clear();
        AllBlob.addAll(set);
        //根据7种merge情况筛选需要增加、删除、保留的文件
        Map<String,String> split_blobs = split_commit.getblobID_map();
        Map<String,String>curr_blobs = currCommit.getblobID_map();
        Map<String,String> merge_blobs = mergeCommit.getblobID_map();
        Map<String,String> map= merge_file(AllBlob,split_blobs,curr_blobs,merge_blobs);
        //message
        String curr_branch = readContentsAsString(HEAD_FILE);
        String message = "Merged "+ branch + " into "+ curr_branch+".";
        //创建新commit并保存
        List<String> parents = new ArrayList<>();
        parents.add(currCommit.getID());
        parents.add(mergeCommit.getID());
        Commit newCommit =new Commit(message,map,parents);
        newCommit.save();
        //变更工作区文件
        change_commit(newCommit);
        //case3-2,在这里才进行是为了防止误删
        deal_conflict(split_blobs,curr_blobs,merge_blobs);
        //保存新commit
        saveHeads(newCommit);


    }
    private static Map<String, String> merge_file_map(List<String> add_blobsID,List<String> overwrite_blobsID,List<String> delete_blobsID,Commit commit){
        Map<String, String> mergedCommitBlobs = commit.getblobID_map();
        if (!add_blobsID.isEmpty()){
            for (String i: add_blobsID){
                Blob blob = Blob.getblob_byID(i);
                mergedCommitBlobs.put(blob.get_path(),blob.getID());
            }
        }
        if (!overwrite_blobsID.isEmpty()){
            for (String i: overwrite_blobsID){
                Blob blob = Blob.getblob_byID(i);
                //这里操作和add一样是因为put时候，是对应路径放的，它会把原来那个文件覆盖
                mergedCommitBlobs.put(blob.get_path(),blob.getID());
            }
        }
        if (!delete_blobsID.isEmpty()){
            for (String i: delete_blobsID){
                Blob blob = Blob.getblob_byID(i);
                mergedCommitBlobs.remove(blob.get_path());
            }
        }
        return mergedCommitBlobs;
    }


}
