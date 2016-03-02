
package com.oguzhandongul.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

public class EmailAutoCompleteTextView extends AutoCompleteTextView {

    private MailsAutoCompleteAdapter newQueryAdapter;
    private ArrayList<String> queryResultList = new ArrayList<>();
    private ArrayList<String> domains = new ArrayList<>();

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public EmailAutoCompleteTextView(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmailAutoCompleteTextView);
            CharSequence[] entries = a.getTextArray(R.styleable.EmailAutoCompleteTextView_android_entries);
            boolean isSuggestionEnabled = a.getBoolean(R.styleable.EmailAutoCompleteTextView_eactSuggestion, true);
            if (entries != null) {
                //creates new domain list from xml attribute
                domains = new ArrayList<>();
                for (int i = 0; i < entries.length; i++) {
                    domains.add(entries[i].toString());
                }
            }
            a.recycle();

            //inits & sets adapter
            newQueryAdapter = new MailsAutoCompleteAdapter(getContext(), android.R.layout.simple_list_item_1);
            setAdapter(newQueryAdapter);
            setThreshold(1);

            //adds text watcher
            addTextChangedListener(watcher);

            //sets single line true
            setSingleLine(true);

            //enables/disables suggestions
            //default is enabled
            setSuggestion(isSuggestionEnabled);
        }
    }

    /**
     * @return  gets the result domain list.
     */
    private ArrayList<String> getList() {
        return queryResultList;
    }


    /**
     * ArrayAdapter which filters result of domain list
     */
    private class MailsAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList = new ArrayList<>();

        public MailsAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = getList();
                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    /**
     * TextWatcher updates list of autocomplete domains on text changed every stroke
     */
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            //clears the query result before updating results
            queryResultList.clear();

            try {
                if (s.length() > 1 && getText().toString().endsWith("@") && canListDomains()) {
                    //if user press @, merges users input and mail domains
                    for (int i = 0; i < domains.size(); i++) {
                        queryResultList.add(getText().toString() + domains.get(i));
                    }

                } else if (s.length() > 1 && getText().toString().contains("@") && canListDomains()) {
                    //if user starts to write mail domain name, finds related domains and filters
                    String currentString = getText().toString();
                    String[] separated = currentString.split("@");
                    String suffix = separated[1];

                    for (int i = 0; i < domains.size(); i++) {
                        if (domains.get(i).startsWith(suffix)) {
                            queryResultList.add(separated[0] + "@" + domains.get(i));
                        }
                    }

                }

                //updates list
                newQueryAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * @param arrayList The ArrayList to use for domains list.
     */
    public void setDomains(ArrayList<String> arrayList) {
        domains = new ArrayList<>(arrayList);
    }

    /**
     * @return if user enters '@' multiple times and prevents any mistaken suggestions, returns false. Otherwise returns true.
     *
     */
    private boolean canListDomains() {
        String s = getText().toString();
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '@') {
                counter++;
            }
            if (counter >= 2) {
                return false;
            }
        }
        return true;
    }

    /**
     *  @param isSuggestionEnabled The boolean attribute value to 'enable/disable' suggestions for input.
     *                             default is 'enabled'
     */
    public void setSuggestion(boolean isSuggestionEnabled){
        if (isSuggestionEnabled) {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
    }
}
