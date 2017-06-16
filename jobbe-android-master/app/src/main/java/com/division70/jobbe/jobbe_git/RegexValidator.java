package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

/**
 * Created by giorgio on 04/12/15.
 */
public class RegexValidator {

    private String urlRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private Context context;

    public RegexValidator(Context context){
        this.context = context;
    }

    public boolean validate(String input){

        String[] array = input.split(" ");

        for(String word : array){
            if(word.matches(Patterns.WEB_URL.pattern())) {
                makeToast(0);
                return false;
            }
            if(word.length() > 5 && word.matches(Patterns.PHONE.pattern())) {
                makeToast(1);
                return false;
            }
            if(word.matches(Patterns.EMAIL_ADDRESS.pattern())) {
                makeToast(2);
                return false;
            }
        }


        return true;
    }

    private void makeToast(int index){
        switch(index){
            case 0:
                Toast.makeText(this.context, "I link non sono permessi, rimuovili per proseguire",
                        Toast.LENGTH_LONG).show();
                break;
            case 1:
                Toast.makeText(this.context, "I numeri di telefono non sono permessi, rimuovili per proseguire",
                        Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(this.context, "Gli indirizzi email non sono permessi, rimuovili per proseguire",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

}
