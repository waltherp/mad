package osmi.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import osmi.todo.entities.TodoEntity;
import osmi.todo.helper.TodoAsyncHelper;
import osmi.todo.helper.TodoDbHelperAsync;
import osmi.todo.helper.TodoPojo;
import osmi.todo.helper.TodoPojoOutPut;
import osmi.todo.helper.TodoPojoParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An activity representing a list of Todos. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TodoDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TodoListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    RecyclerView recyclerView;
    private boolean isOnline = true;
//    TodoAsyncHelper todoAsyncHelperGetAll;
    TodoDbHelperAsync todoDbHelperAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

//        todoDbHelperAsync = new TodoDbHelper(this);

//        todoAsyncHelperGetAll = new TodoAsyncHelper(this, TodoDbHelperAsync.ACTION_GET_ALL, isOnline);

        todoDbHelperAsync = new TodoDbHelperAsync(this, isOnline);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TodoDetailActivity.class);
                context.startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.todo_list);

        if (findViewById(R.id.todo_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        todoDbHelperAsync.sync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sort_date:
                List<TodoEntity> response = todoDbHelperAsync.getAllLocalWithSortBy(TodoDbHelperAsync.COL_FINALDATE);
                TodoListViewAdapter adapter = new TodoListViewAdapter(response);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.item_sort_fav:
                response = todoDbHelperAsync.getAllLocalWithSortBy(TodoDbHelperAsync.COL_FAV);
                adapter = new TodoListViewAdapter(response);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        registerReceiver(receiverGetAll, new IntentFilter(TodoDbHelperAsync.ACTION_GET_ALL));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(receiverGetAll);
    }

    private BroadcastReceiver receiverGetAll = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            TodoPojo todoPojo = (TodoPojo) intent.getSerializableExtra(TodoDbHelperAsync.HTTP_RESPONSE);
            List<TodoEntity> response = todoPojo.getAll;
            ((RecyclerView)recyclerView).setAdapter(new TodoListViewAdapter(response));
        }
    };

    public class TodoListViewAdapter extends RecyclerView.Adapter<TodoListViewAdapter.ViewHolder> {
        private final List<TodoEntity> todos;

        public TodoListViewAdapter(List<TodoEntity> todos) {
            this.todos = todos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.todo_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.todoEntity = todos.get(position);
            holder.nameTextView.setText(todos.get(position).getName());
            Date finalDate = todos.get(position).getFinalDate();
            if(finalDate.getTime() < System.currentTimeMillis()) {
                holder.nameTextView.setBackgroundColor(getResources().getColor(R.color.colorExpired));
            }
            holder.finalDateTextView.setText(finalDate.toString());
            holder.solvedCheckBox.setChecked(todos.get(position).isSolved());
            holder.favCheckBox.setChecked(todos.get(position).isFav());

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(TodoDetailFragment.ARG_ITEM_ID, String.valueOf(holder.todoEntity.getId()));
                        TodoDetailFragment fragment = new TodoDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.todo_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, TodoDetailActivity.class);
                        intent.putExtra(TodoDetailFragment.ARG_ITEM_ID, String.valueOf(holder.todoEntity.getId()));

                        context.startActivity(intent);
                    }
                }
            });
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean delete = todoDbHelperAsync.delete(holder.todoEntity);
                    if(delete) {
                        todos.remove(position);
                        notifyDataSetChanged();
                    }
                    return delete;
                }
            });
            holder.solvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    holder.todoEntity.setSolved(isChecked);
                    holder.todoEntity = todoDbHelperAsync.update(holder.todoEntity);
                }
            });
            holder.favCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    holder.todoEntity.setFav(isChecked);
                    holder.todoEntity = todoDbHelperAsync.update(holder.todoEntity);
                }
            });
        }

        @Override
        public int getItemCount() {
            return todos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View view;
            public final TextView nameTextView;
            public final TextView finalDateTextView;
            public final CheckBox solvedCheckBox;
            public final CheckBox favCheckBox;
            public TodoEntity todoEntity;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                nameTextView = (TextView) view.findViewById(R.id.todoNameList);
                finalDateTextView = (TextView) view.findViewById(R.id.todoFinalDateList);
                solvedCheckBox = (CheckBox) view.findViewById(R.id.todoSolvedList);
                favCheckBox = (CheckBox) view.findViewById(R.id.todoFavList);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + nameTextView.getText() + "'";
            }
        }
    }

}
