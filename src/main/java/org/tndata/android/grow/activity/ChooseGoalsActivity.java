package org.tndata.android.grow.activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.tndata.android.grow.GrowApplication;
import org.tndata.android.grow.R;
import org.tndata.android.grow.model.Category;
import org.tndata.android.grow.model.Goal;
import org.tndata.android.grow.task.AddGoalTask;
import org.tndata.android.grow.task.DeleteGoalTask;
import org.tndata.android.grow.task.GoalLoaderTask;
import org.tndata.android.grow.ui.SpacingItemDecoration;
import org.tndata.android.grow.ui.parallaxrecyclerview.HeaderLayoutManagerFixed;
import org.tndata.android.grow.ui.parallaxrecyclerview.ParallaxRecyclerAdapter;
import org.tndata.android.grow.util.ImageCache;
import org.tndata.android.grow.util.ImageHelper;

import java.util.ArrayList;


public class ChooseGoalsActivity extends ActionBarActivity implements AddGoalTask
        .AddGoalsTaskListener,
        GoalLoaderTask.GoalLoaderListener, DeleteGoalTask.DeleteGoalTaskListener {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mHeaderTextView;
    private TextView mErrorTextView;
    private View mFakeHeader;
    private View mHeaderView;
    private ArrayList<Goal> mItems;
    private ArrayList<Goal> mSelectedGoals = new ArrayList<Goal>();
    private ParallaxRecyclerAdapter<Goal> mAdapter;
    private Category mCategory = null;
    private boolean mAdding = false;
    private boolean mDeleting = false;
    private ArrayList<Integer> mExpandedPositions = new ArrayList<Integer>();

    static class ChooseGoalViewHolder extends RecyclerView.ViewHolder {
        public ChooseGoalViewHolder(View itemView) {
            super(itemView);
            iconImageView = (ImageView) itemView
                    .findViewById(R.id.list_item_choose_goal_icon_imageview);
            iconContainerView = (RelativeLayout) itemView.findViewById(R.id
                    .list_item_choose_goal_imageview_container);
            titleTextView = (TextView) itemView
                    .findViewById(R.id.list_item_choose_goal_title_textview);
            selectButton = (ImageView) itemView
                    .findViewById(R.id.list_item_choose_goal_select_button);
            descriptionTextView = (TextView) itemView
                    .findViewById(R.id.list_item_choose_goal_description_textview);
            detailContainerView = (RelativeLayout) itemView.findViewById(R.id
                    .list_item_choose_goal_detail_container);
        }

        TextView titleTextView;
        TextView descriptionTextView;
        ImageView iconImageView;
        ImageView selectButton;
        RelativeLayout iconContainerView;
        RelativeLayout detailContainerView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_goals);

        mCategory = (Category) getIntent().getSerializableExtra("category");

        mToolbar = (Toolbar) findViewById(R.id.choose_goals_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        mToolbar.setTitle(getString(R.string.choose_goals_header_label,
                mCategory.getTitle()));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.choose_goals_recyclerview);
        HeaderLayoutManagerFixed manager = new HeaderLayoutManagerFixed(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(new SpacingItemDecoration(30));
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mErrorTextView = (TextView) findViewById(R.id.choose_goals_error_textview);

        mItems = new ArrayList<Goal>();
        mAdapter = new ParallaxRecyclerAdapter<>(mItems);
        mAdapter.implementRecyclerAdapterMethods(new ParallaxRecyclerAdapter
                .RecyclerAdapterMethods() {
            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,
                                         final int i) {
                final Goal goal = mItems.get(i);

                ((ChooseGoalViewHolder) viewHolder).titleTextView.setText(goal
                        .getTitle());
                if (mExpandedPositions.contains(Integer.valueOf(i))) {
                    ((ChooseGoalViewHolder) viewHolder).descriptionTextView.setVisibility(View
                            .VISIBLE);
                } else {
                    ((ChooseGoalViewHolder) viewHolder).descriptionTextView.setVisibility(View
                            .GONE);
                }
                ((ChooseGoalViewHolder) viewHolder).descriptionTextView.setText(goal
                        .getDescription());
                if (goal.getIconUrl() != null
                        && !goal.getIconUrl().isEmpty()) {
                    ImageCache.instance(getApplicationContext()).loadBitmap(
                            ((ChooseGoalViewHolder) viewHolder).iconImageView,
                            goal.getIconUrl(), false);
                }
                if (mSelectedGoals.contains(goal)) {
                    ImageHelper.setupImageViewButton(getResources(),
                            ((ChooseGoalViewHolder) viewHolder).selectButton, ImageHelper.SELECTED);
                } else {
                    ImageHelper.setupImageViewButton(getResources(),
                            ((ChooseGoalViewHolder) viewHolder).selectButton, ImageHelper.ADD);
                }

                ((ChooseGoalViewHolder) viewHolder).selectButton.setOnClickListener(new View
                        .OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        goalSelected(goal);
                    }
                });
                ((ChooseGoalViewHolder) viewHolder).detailContainerView.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {
                        ((ChooseGoalViewHolder) viewHolder).descriptionTextView.setVisibility
                                (View.VISIBLE);
                        mExpandedPositions.add(Integer.valueOf(i));
                    }
                });


                GradientDrawable gradientDrawable = (GradientDrawable) ((ChooseGoalViewHolder)
                        viewHolder)
                        .iconContainerView.getBackground();
                String colorString = mCategory.getColor();
                Log.d("color", colorString);
                if (colorString != null && !colorString.isEmpty()) {
                    gradientDrawable.setColor(Color.parseColor(colorString));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ((ChooseGoalViewHolder) viewHolder).iconContainerView.setBackground
                                (gradientDrawable);
                    } else {
                        ((ChooseGoalViewHolder) viewHolder).iconContainerView
                                .setBackgroundDrawable(gradientDrawable);
                    }
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(
                    ViewGroup viewGroup, int i) {
                return new ChooseGoalViewHolder(getLayoutInflater().inflate(
                        R.layout.list_item_choose_goal, viewGroup, false));
            }

            @Override
            public int getItemCount() {
                return mItems.size();
            }
        });

        mFakeHeader = getLayoutInflater().inflate(R.layout.header_choose_goals,
                mRecyclerView, false);
        mHeaderTextView = (TextView) mFakeHeader.findViewById(R.id
                .choose_goals_header_label_textview);
        mHeaderTextView.setText(mCategory.getDescription());
        mHeaderView = findViewById(R.id.choose_goals_material_view);
        manager.setHeaderIncrementFixer(mFakeHeader);
        mAdapter.setShouldClipView(false);
        mAdapter.setParallaxHeader(mFakeHeader, mRecyclerView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAdapter.setOnParallaxScroll(new ParallaxRecyclerAdapter.OnParallaxScroll() {
                @SuppressLint("NewApi")
                @Override
                public void onParallaxScroll(float percentage, float offset,
                                             View parallax) {
                    Drawable c = mToolbar.getBackground();
                    c.setAlpha(Math.round(percentage * 255));
                    mToolbar.setBackground(c);
                    mHeaderView.setTranslationY(-offset * 0.5f);

                }
            });
        }
        mRecyclerView.setAdapter(mAdapter);

        if (mCategory != null && !mCategory.getColor().isEmpty()) {
            mHeaderView.setBackgroundColor(Color.parseColor(mCategory.getColor()));
            mToolbar.setBackgroundColor(Color.parseColor(mCategory.getColor()));
        }

        if (mCategory.getGoals() != null) {
            mSelectedGoals.addAll(mCategory.getGoals());
        }

        loadGoals();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { // Back key pressed
            goalsSelected(mSelectedGoals);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goalsSelected(mSelectedGoals);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showError() {
        mRecyclerView.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("DefaultLocale")
    private void loadGoals() {
        if (mCategory == null) {
            return;
        }
        new GoalLoaderTask(getApplicationContext(), this).executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR,
                ((GrowApplication) getApplication()).getToken(),
                String.valueOf(mCategory.getId()));
    }

    @Override
    public void goalsAdded(ArrayList<Goal> goals) {
        if (goals != null) {
            ArrayList<Goal> allGoals = ((GrowApplication) getApplication()).getGoals();
            allGoals.addAll(goals);
            ((GrowApplication) getApplication()).setGoals(allGoals);
        }
        mAdding = false;
        if (!mDeleting) {
            finish();
        }
    }

    public void goalSelected(Goal goal) {

        if (mSelectedGoals.contains(goal)) {
            mSelectedGoals.remove(goal);
        } else {
            mSelectedGoals.add(goal);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void moreInfoPressed(Goal goal) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChooseGoalsActivity.this);

            builder.setMessage(goal.getDescription()).setTitle(goal.getTitle());
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void goalsSelected(ArrayList<Goal> goals) {
        Log.e("GOALS", "GOALS SELECTED");
        //Lets just save the new ones...
        ArrayList<Goal> goalsWithDelete = new ArrayList<Goal>();
        goalsWithDelete.addAll(((GrowApplication) getApplication()).getGoals());
        ArrayList<String> deleteGoals = new ArrayList<String>();
        ArrayList<Goal> goalsToAdd = new ArrayList<Goal>();
        for (Goal goal : mCategory.getGoals()) {
            Log.d("SHOULD DELETE?", goal.getTitle());
            if (!goals.contains(goal)) {
                Log.d("Delete Goal", goal.getTitle());
                deleteGoals.add(String.valueOf(goal.getMappingId()));
                goalsWithDelete.remove(goal);
            }
        }
        for (Goal goal : goals) {
            Log.d("SHOULD ADD?", goal.getTitle());
            if (!mCategory.getGoals().contains(goal)) {
                Log.d("Add Goal", goal.getTitle());
                goalsToAdd.add(goal);
            }
        }

        ((GrowApplication) getApplication()).setGoals(goalsWithDelete);

        if (deleteGoals.size() > 0) {
            mDeleting = true;
            new DeleteGoalTask(this, this, deleteGoals).executeOnExecutor(AsyncTask
                    .THREAD_POOL_EXECUTOR);
        }

        ArrayList<String> goalList = new ArrayList<String>();
        for (Goal goal : goalsToAdd) {
            goalList.add(String.valueOf(goal.getId()));
        }
        if (goalList.size() > 0) {
            mAdding = true;
            new AddGoalTask(this, this, goalList)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (deleteGoals.size() < 1) {
            finish();
        }
    }

    @Override
    public void goalLoaderFinished(ArrayList<Goal> goals) {
        if (goals != null && !goals.isEmpty()) {
            mItems.addAll(goals);
            mAdapter.notifyDataSetChanged();
        } else {
            showError();
        }
    }

    @Override
    public void goalsDeleted() {
        mDeleting = false;

        if (!mAdding) {
            finish();
        }
    }
}
