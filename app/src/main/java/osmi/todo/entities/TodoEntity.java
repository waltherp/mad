package osmi.todo.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by patri on 12.06.2016.
 */
public final class TodoEntity implements Serializable{
    public TodoEntity() {
    }
    private int id;
    private String name;
    private String desc;
    private boolean solved;
    private boolean fav;

    public Date getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(Date finalDate) {
        this.finalDate = finalDate;
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private Date finalDate;

//    public static final String COL_ID = "ID";
//    public static final String COL_NAME = "NAME";
//    public static final String COL_DESC = "DESC";
//    public static final String COL_SOLVED = "SOLVED";
//    public static final String COL_FAV = "FAV";
//    public static final String COL_FINALDATE = "FINALDATE";
}
