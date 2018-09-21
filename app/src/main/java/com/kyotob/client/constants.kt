package com.kyotob.client

const val baseUrl = "http://ec2-54-238-183-251.ap-northeast-1.compute.amazonaws.com/"
const val baseIP = "ec2-54-238-183-251.ap-northeast-1.compute.amazonaws.com"

const val USER_DATA_KEY = "userData"
const val USER_NAME_KEY = "userName"
const val USER_SCREEN_NAME_KEY = "screenName"
const val TOKEN_KEY = "accessToken"
// Activity間のデータ受け渡し用キー
const val EXTRA_MESSAGE = "com.kyotob.client.MESSAGE"
// 画像アップロード用定数
const val TAKE_PICTURE = 1
const val SELECT_PICTURE = 2
const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1