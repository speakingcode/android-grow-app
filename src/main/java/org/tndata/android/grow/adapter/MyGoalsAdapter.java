package org.tndata.android.grow.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.tndata.android.grow.R;
import org.tndata.android.grow.fragment.SurveyDialogFragment;
import org.tndata.android.grow.model.Category;
import org.tndata.android.grow.model.Goal;
import org.tndata.android.grow.model.MyGoalsViewItem;
import org.tndata.android.grow.model.Survey;
import org.tndata.android.grow.model.SurveyOptions;
import org.tndata.android.grow.ui.GoalCellView;
import org.tndata.android.grow.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MyGoalsAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> implements SurveyDialogFragment
        .SurveyDialogListener {
    public interface OnClickEvent {
        void onClick(View v, int position);
    }

    public interface MyGoalsAdapterInterface {
        public void surveyCompleted(Survey survey);

        public void chooseGoals(Category category);

        public void chooseBehaviors(Goal goal, Category category);
    }

    private Context mContext;
    private List<MyGoalsViewItem> mItems;
    private MyGoalsAdapterInterface mCallback;

    static class MyGoalsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView subTitleTextView;
        RelativeLayout circleView;
        LinearLayout noGoalsContainer;
        LinearLayout goalContainer;

        public MyGoalsViewHolder(View view) {
            super(view);
            circleView = (RelativeLayout) view
                    .findViewById(R.id.list_item_my_goals_category_circle_view);
            titleTextView = (TextView) view
                    .findViewById(R.id.list_item_my_goals_category_title_textview);
            subTitleTextView = (TextView) view
                    .findViewById(R.id.list_item_my_goals_category_add_textview);
            noGoalsContainer = (LinearLayout) view.findViewById(R.id
                    .list_item_my_goals_category_no_goals_container);
            goalContainer = (LinearLayout) view.findViewById(R.id
                    .list_item_my_goals_category_goals_container);
        }
    }

    static class MyGoalsNoContentViewHolder extends RecyclerView.ViewHolder {
        public MyGoalsNoContentViewHolder(View view) {
            super(view);
        }
    }

    static class SurveyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout surveyContainer;

        public SurveyViewHolder(View view) {
            super(view);
            surveyContainer = (LinearLayout) view.findViewById(R.id.list_item_survey_container);
        }
    }

    public MyGoalsAdapter(Context context, List<MyGoalsViewItem> objects,
                          MyGoalsAdapterInterface callback) {
        if (objects == null) {
            throw new IllegalArgumentException("Goals List must not be null");
        }
        this.mItems = objects;
        this.mContext = context;
        this.mCallback = callback;
    }

    public void updateEntries(List<MyGoalsViewItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,
                                 final int position) {
        final Survey survey = mItems.get(position).getSurvey();
        switch (viewHolder.getItemViewType()) {
            case MyGoalsViewItem.TYPE_DEFAULT_NO_CONTENT:
                break;
            case MyGoalsViewItem.TYPE_CATEGORY:
                final Category category = mItems.get(position).getCategory();
                ArrayList<Goal> goals = category.getGoals();
                ((MyGoalsViewHolder) viewHolder).titleTextView.setText(mContext.getString(R
                        .string.category_goals, category.getTitle()));
                ((MyGoalsViewHolder) viewHolder).goalContainer.removeAllViews();
                if (goals != null && !goals.isEmpty()) {
                    ((MyGoalsViewHolder) viewHolder).noGoalsContainer.setVisibility(View.GONE);
                    for (final Goal goal : goals) {
                        GoalCellView goalCellView = new GoalCellView(mContext);
                        goalCellView.setGoal(goal, category);
                        goalCellView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCallback.chooseBehaviors(goal, category);
                            }
                        });
                        ((MyGoalsViewHolder) viewHolder).goalContainer.addView(goalCellView);
                    }
                } else {
                    GradientDrawable gradientDrawable = (GradientDrawable) ((MyGoalsViewHolder)
                            viewHolder).circleView.getBackground();
                    String colorString = category.getColor();
                    if (colorString != null && !colorString.isEmpty()) {
                        gradientDrawable.setColor(Color.parseColor(colorString));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ((MyGoalsViewHolder) viewHolder).circleView.setBackground
                                (gradientDrawable);
                    } else {
                        ((MyGoalsViewHolder) viewHolder).circleView
                                .setBackgroundDrawable(gradientDrawable);
                    }
                    ((MyGoalsViewHolder) viewHolder).noGoalsContainer.setVisibility(View.VISIBLE);
                    ((MyGoalsViewHolder) viewHolder).subTitleTextView.setText(mContext.getString
                            (R.string.category_goals_add,
                                    category.getTitle()));
                    ((MyGoalsViewHolder) viewHolder).noGoalsContainer.setOnClickListener(new View
                            .OnClickListener() {


                        @Override
                        public void onClick(View v) {
                            mCallback.chooseGoals(category);
                        }
                    });
                }
                break;

            case MyGoalsViewItem.TYPE_SURVEY_LIKERT:
            case MyGoalsViewItem.TYPE_SURVEY_MULTICHOICE:
            case MyGoalsViewItem.TYPE_SURVEY_BINARY:
            case MyGoalsViewItem.TYPE_SURVEY_OPENENDED:

                ((SurveyViewHolder) viewHolder).surveyContainer.removeAllViews();
                SurveyDialogFragment fragment = SurveyDialogFragment.newInstance(survey, false,
                        true);
                fragment.setListener(this);
                ((Activity) mContext).getFragmentManager().beginTransaction().add((
                                (SurveyViewHolder) viewHolder).surveyContainer.getId(), fragment,
                        "survey").commit();
                break;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView;
        switch (viewType) {

            case MyGoalsViewItem.TYPE_CATEGORY:
                itemView = inflater.inflate(
                        R.layout.list_item_my_goals_category, viewGroup, false);
                return new MyGoalsViewHolder(itemView);
            case MyGoalsViewItem.TYPE_DEFAULT_NO_CONTENT:
                itemView = inflater.inflate(
                        R.layout.list_item_default_no_content, viewGroup, false);
                return new MyGoalsNoContentViewHolder(itemView);
            case MyGoalsViewItem.TYPE_SURVEY_LIKERT:
            case MyGoalsViewItem.TYPE_SURVEY_MULTICHOICE:
            case MyGoalsViewItem.TYPE_SURVEY_BINARY:
            case MyGoalsViewItem.TYPE_SURVEY_OPENENDED:
                itemView = inflater.inflate(R.layout.list_item_survey, viewGroup, false);
                return new SurveyViewHolder(itemView);
            default:
                itemView = inflater.inflate(
                        R.layout.list_item_goal, viewGroup, false);

                return new MyGoalsViewHolder(itemView);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public void onDialogPositiveClick(Survey survey) {
        mCallback.surveyCompleted(survey);
    }

    @Override
    public void onDialogNegativeClick(Survey survey) {
        //not needed in this fragment
    }

    @Override
    public void onDialogCanceled() {
        //not needed in this fragment
    }


    @Override
    public void setNextButtonEnabled(boolean enabled) {
        //not needed in this fragment
    }

}
