package osmi.todo;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Date;

import osmi.todo.entities.TodoEntity;
import osmi.todo.helper.TodoDbHelperAsync;

/**
 * A fragment representing a single Todo detail screen.
 * This fragment is either contained in a {@link TodoListActivity}
 * in two-pane mode (on tablets) or a {@link TodoDetailActivity}
 * on handsets.
 */
public class TodoDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
//    private DummyContent.DummyItem mItem;
    private TodoDbHelperAsync todoDbHelperAsync;
    public TodoEntity todoEntity;

    EditText todoNameEdit;
    EditText todoDescEdit;
    CheckBox todoSolvedCheck;
    CheckBox todoFavCheck;
    CalendarView todoFinalDateCalendar;
    private boolean isOnline = true;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TodoDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        todoDbHelperAsync = new TodoDbHelperAsync(this.getContext(), isOnline);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            String todoId = getArguments().getString(ARG_ITEM_ID);
            if(todoId == null || todoId.isEmpty()) return;
//            mItem = DummyContent.ITEM_MAP.get(todoId);

            todoEntity = todoDbHelperAsync.getById(Integer.parseInt(todoId));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(todoEntity.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.todo_detail, container, false);

        // Show the dummy content as text in a TextView.
        todoNameEdit = (EditText) rootView.findViewById(R.id.todoNameEdit);
        todoDescEdit = (EditText) rootView.findViewById(R.id.todoDescEdit);
        todoSolvedCheck = (CheckBox) rootView.findViewById(R.id.todoSolvedCheck);
        todoFavCheck = (CheckBox) rootView.findViewById(R.id.todoFavCheck);
        todoFinalDateCalendar = (CalendarView) rootView.findViewById(R.id.todoFinalDateCalendar);
        if (todoEntity != null) {

            todoNameEdit.setText(todoEntity.getName());
            todoDescEdit.setText(todoEntity.getDesc());
            todoSolvedCheck.setChecked(todoEntity.isSolved());
            todoFavCheck.setChecked(todoEntity.isFav());
            todoFinalDateCalendar.setDate(todoEntity.getFinalDate().getTime());
        }

        return rootView;
    }

    public boolean save() {
        String name = todoNameEdit.getText().toString();
        String desc = todoDescEdit.getText().toString();
        boolean solved = todoSolvedCheck.isChecked();
        boolean fav = todoFavCheck.isChecked();
        Date finalDate = new Date(todoFinalDateCalendar.getDate());

        if(todoEntity == null) todoEntity = new TodoEntity();

        todoEntity.setName(name);
        todoEntity.setDesc(desc);
        todoEntity.setSolved(solved);
        todoEntity.setFav(fav);
        todoEntity.setFinalDate(finalDate);

        todoEntity = todoDbHelperAsync.create(todoEntity);

        return todoEntity != null;
    }
}
