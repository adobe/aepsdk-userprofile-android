/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.mobile.marketing.userprofile.testapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.UserProfile
import com.adobe.mobile.marketing.userprofile.testapp.ui.theme.AepsdkuserprofileandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AepsdkuserprofileandroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Input()
                }
            }
        }
    }
}

@Composable
fun Input() {
    val sharedPreference = LocalContext.current.getSharedPreferences("ADBUserProfile", 0)
    val sharedPreferenceEditor = sharedPreference.edit()
    val keyState = remember { mutableStateOf(TextFieldValue()) }
    val valueState = remember { mutableStateOf(TextFieldValue()) }
    val getterResult = remember {
        mutableStateOf("")
    }
    val kvPairsLoadedState = remember {
        mutableStateOf(mapOf<String, Any?>())
    }
    val getterResultState = remember {
        mutableStateOf(false)
    }
    Column(Modifier.padding(8.dp)) {
        Row {

            TextField(
                value = keyState.value,
                onValueChange = { keyState.value = it },
                label = { Text(text = "key") }
            )
        }
        Row {
            TextField(
                value = valueState.value,
                onValueChange = { valueState.value = it },
                label = { Text(text = "value") }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Button(onClick = {
                update(keyState.value.text, valueState.value.text)
            }) {
                Text("Update")
            }
            Spacer(modifier = Modifier.width(2.dp))
            Button(onClick = {
                remove(keyState.value.text)
            }) {
                Text("Remove")
            }
            Spacer(modifier = Modifier.width(2.dp))
            Button(onClick = {
                val key = keyState.value.text
                UserProfile.getUserAttributes(listOf(key)) { map ->
                    val value = map[key]
                    getterResultState.value = true
                    getterResult.value = "$key: $value"
                }
            }) {
                Text("Get")
            }
            Spacer(modifier = Modifier.width(2.dp))
            Button(onClick = {
                kvPairsLoadedState.value = load(sharedPreference)
            }) {
                Text("Load")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            sharedPreferenceEditor.clear()
            sharedPreferenceEditor.commit()
        }) {
            Text("Clear")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column {
            for ((k, v) in kvPairsLoadedState.value) {
                Text("$k=$v;")
            }
        }
        Box {
            if (getterResultState.value) {
                AlertDialog(
                    onDismissRequest = {
                        getterResultState.value = false
                    },
                    title = {
                        Text(text = "Title")
                    },
                    text = {
                        Column {
                            Text("result: ${getterResult.value}")
                        }
                    },
                    buttons = {
                        Row(
                            modifier = Modifier.padding(all = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { getterResultState.value = false }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                )
            }
        }
    }
}

fun load(sharedPreferences: SharedPreferences): Map<String, Any?> {
    return sharedPreferences.all
}

fun update(key: String, value: String) {
    val attributeMap = mapOf(key to value)
    UserProfile.updateUserAttributes(attributeMap);
}

fun remove(key: String) {
    val attributeNames = listOf(key)
    UserProfile.removeUserAttributes(attributeNames)
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AepsdkuserprofileandroidTheme {
        Input()
    }
}