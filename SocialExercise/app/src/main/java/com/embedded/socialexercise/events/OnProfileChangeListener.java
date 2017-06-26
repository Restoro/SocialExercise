package com.embedded.socialexercise.events;

import com.embedded.socialexercise.person.Person;

/**
 * Created by Verena on 26.06.2017.
 */

public interface OnProfileChangeListener {
    void profileChanged(Person p);
}
