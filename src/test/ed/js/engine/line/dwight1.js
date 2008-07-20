/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

print( "" );


// assuming HTTP_HOST is always getting set (cron is going to do this from now on)
parts = explode('.', _server()['HTTP_HOST']);
site_domain =  implode('.', array_slice(parts, sizeof(parts) - 2));
main_uri =  'http://' + site_domain;
static_uri =  'http://static.' + site_domain;
forums_uri =  'http://forums.' + site_domain;
stats_uri =  'http://st.' + site_domain;

/***** LOCALE-SPECIFIC SETTINGS ******/
// These settings cannot be apc-cached because we are hosting multiple locales on the same
// machine
if (
    (strpos(_server()['HTTP_HOST'], 'musicnation-sandbox.fr') !== false) ||
    (strpos(_server()['HTTP_HOST'], 'musicnation.fr') !== false) ||
    (strpos(_server()['HTTP_HOST'], 'musicnationfrance.com') !== false)
) {
    locale =  'fr_FR';
    language =  'fr';
    timezone =  'Europe/Paris';
    db_master_name =  'musicnation_fr';
    db_slave_name =  'musicnation_fr';
    db_stats_master_name =  'musicnation_stats_fr';
    sailthru_api_key =  'b24b09113c844de4edc2dda5db1efc74';
    sailthru_secret =   '6451842de1afbe4e23954c27eabddd1b';
    music_nation_user_id =  1;
    enable_blogs =  1;
    enable_myspace =  1;
    enable_homepage =  1;
    translate_reverse =  0;
    verify_mode =  'email';
} else {
    locale =  'en_US';
    language =  'en';
    timezone =  'America/New_York';
    db_master_name =  'musicnation';
    db_slave_name =  'musicnation';
    db_stats_master_name =  'musicnation_stats';
    sailthru_api_key =  'bc68a4b8d030fee1083d9323115193ef';
    sailthru_secret =   '0a18014162ef7315dba4b62ae718865e';
    music_nation_user_id =  1435;
    enable_blogs =  1;
    enable_myspace =  1;
    enable_homepage =  1;
    translate_reverse =  0;
    verify_mode =  'email';
}

if (strpos(_server()['HTTP_HOST'], 'sandbox') !== false) {
    
    google_maps_key =  "ABQIAAAAzPyBJE22uGEClGT2cVj_0BSPYiK_0-xAmU_N5aDrr7aW1ha7YxTKQc2njNWkcp1yKmGxUpzNdgfpgw";
    
 } else if(strpos(_server()['HTTP_HOST'], 'branch') !== false) {
    google_maps_key =  "ABQIAAAAzPyBJE22uGEClGT2cVj_0BRd1G88sUEr6vezEZpiEUt7QKM1HRQo9-ifhveUdvabHQ7HFgkp3FxZJQ";
    
 } else {
	google_maps_key =  "ABQIAAAAzPyBJE22uGEClGT2cVj_0BSdT49nAA9bLPgXu1LZ_Ik9kK12YxT8M8mZH8Pl6JeYe8dkEKLDPMHGxQ";
 }


/****** OTHER CONSTANTS ******/
// the constants that follow are all specific to the machine we're running on, but not the vhost
print("<pre>K\n");

if (!apc_load_constants('global') || isset(_get()['reload_apc'])) {
    print("L\n");
    constants = [];
	constants['BRANCH'] = 0;

    // decide whether we are in local/dev/prod mode depending on HTTP_HOST
    if (strpos(_server()['HTTP_HOST'], 'sandbox') !== false) {
        constants['ENVIRONMENT_NAME'] = 'local';
	
    } else if(strpos(_server()['HTTP_HOST'], 'branch') !== false) {
    	constants['BRANCH'] = 1;
        constants['ENVIRONMENT_NAME'] = 'local';
    } else if (
        (strpos(_server()['HTTP_HOST'], 'fabricmg') !== false) ||
        (strpos(_server()['HTTP_HOST'], 'cmnation') !== false)
       ) {
       constants['ENVIRONMENT_NAME'] = 'dev';
    } else {
        constants['ENVIRONMENT_NAME'] = 'prod';
    }

    constants['UPLOAD_DIR'] = '/fmgmedia/upload/';
    constants['UPLOAD_SHARE_DIR'] = '/fmgmedia/new/';
    constants['CURRENT_STORAGE_DIR'] = '/fmgmedia/media1/';
    constants['BASE_MEDIA_FILE_PATH'] = '/fmgmedia';
    constants['AMAZON_S3_MEDIA'] = 0;

    switch (constants['ENVIRONMENT_NAME']) {
        case 'local':
            constants['DB_MASTER_HOSTNAME'] = 'localhost';
            constants['DB_MASTER_USERNAME'] = 'cdbu';
            constants['DB_MASTER_PASSWORD'] = 'cdbu';

            constants['DB_SLAVE_HOSTNAME'] = 'localhost';
            constants['DB_SLAVE_USERNAME'] = 'cdbu';
            constants['DB_SLAVE_PASSWORD'] = 'cdbu';

            constants['DB_STATS_MASTER_HOSTNAME'] = 'localhost';
            constants['DB_STATS_MASTER_USERNAME'] = 'cdbu';
            constants['DB_STATS_MASTER_PASSWORD'] = 'cdbu';

            if (isset(_get()['local'])) {
                constants['VIDEO_DOMAIN'] = 'vmnation-sandbox.com';
            } else {
                constants['VIDEO_DOMAIN'] = 'panther.vmnation.com';
            }

            constants['NODE_NAME'] = 'n1';
            constants['NODE_ID'] = 1;

            //            break;

        case 'dev':
            constants['DB_MASTER_HOSTNAME'] = 'dev1.s.fabricmg.net';
            constants['DB_MASTER_USERNAME'] = 'cdbu';
            constants['DB_MASTER_PASSWORD'] = 'rock0n1122';

            constants['DB_SLAVE_HOSTNAME'] = 'dev1.s.fabricmg.net';
            constants['DB_SLAVE_USERNAME'] = 'cdbu';
            constants['DB_SLAVE_PASSWORD'] = 'rock0n1122';

            constants['DB_STATS_MASTER_HOSTNAME'] = 'dev1.s.fabricmg.net';
            constants['DB_STATS_MASTER_USERNAME'] = 'cdbu';
            constants['DB_STATS_MASTER_PASSWORD'] = 'rock0n1122';

//            $constants['VIDEO_DOMAIN'] = 'vmnation.net';
            constants['VIDEO_DOMAIN'] = 'panther.vmnation.com';

            constants['AMAZON_S3_MEDIA'] = 0;
            
            constants['NODE_NAME'] = 'n1';
            constants['NODE_ID'] = 1;
            //z.z.z.z.z
            //    		break;

        case 'prod':

        	constants['DB_MASTER_HOSTNAME'] = 'n1.db.musicnation.ws';
            constants['DB_MASTER_USERNAME'] = 'mnweb';
            constants['DB_MASTER_PASSWORD'] = 'mysp@c3killer';

            constants['DB_SLAVE_HOSTNAME'] = 'n2.db.musicnation.ws';
            constants['DB_SLAVE_USERNAME'] = 'mnweb_slave';
            constants['DB_SLAVE_PASSWORD'] = 'N3v3rm1nd';

            constants['DB_STATS_MASTER_HOSTNAME'] = 'n1.ss.musicnation.ws';
            constants['DB_STATS_MASTER_USERNAME'] = 'mnstats';
            constants['DB_STATS_MASTER_PASSWORD'] = 'N3v3rm1nd';

            /*
            // this commented-out code will take n2db out of commission if you need to
            $constants['DB_SLAVE_HOSTNAME'] = $constants['DB_MASTER_HOSTNAME'];
            $constants['DB_SLAVE_USERNAME'] = $constants['DB_MASTER_USERNAME'];
            $constants['DB_SLAVE_PASSWORD'] = $constants['DB_MASTER_PASSWORD'];

            $constants['DB_FORUM_SLAVE_HOSTNAME'] = $constants['DB_FORUM_MASTER_HOSTNAME'];
            $constants['DB_FORUM_SLAVE_USERNAME'] = $constants['DB_FORUM_MASTER_USERNAME'];
            $constants['DB_FORUM_SLAVE_PASSWORD'] = $constants['DB_FORUM_MASTER_PASSWORD'];
            */

            constants['VIDEO_DOMAIN'] = 'panther.vmnation.com';

            constants['NODE_NAME'] = '__NODE_NAME__';
            constants['NODE_ID'] = '__NODE_ID__';

    
    }

    constants['VIDEO_URI'] = 'http://' + constants['VIDEO_DOMAIN'];

    if (constants['ENVIRONMENT_NAME'] != 'prod') {
        constants['AMAZON_S3_BUCKET'] = 'musicnation_test';
        constants['AMAZON_S3_KEY'] = '0Z8YSGQC0JPN41XEKNR2';
        constants['AMAZON_S3_SECRET'] = 'UOSuGwl/vDHc6W9K6YY6LT1hasBPZPeu7WcWD3Io';
        constants['PAYPAL_WEBSCR_URL'] = 'https://www.sandbox.paypal.com/cgi-bin/webscr';
        constants['PAYPAL_SELLER_EMAIL'] = 'jasontest@mindread.com';
    } else {
        constants['AMAZON_S3_BUCKET'] = 'musicnation_media';
        constants['AMAZON_S3_KEY'] = '0Z8YSGQC0JPN41XEKNR2';
        constants['AMAZON_S3_SECRET'] = 'UOSuGwl/vDHc6W9K6YY6LT1hasBPZPeu7WcWD3Io';
        constants['PAYPAL_WEBSCR_URL'] = 'https://www.paypal.com/cgi-bin/webscr';
        constants['PAYPAL_SELLER_EMAIL'] = 'paypal@musicnation.com';
    }

    constants['ROOTPATH'] = dirname(dirname('/data/sites/php/version2/configs/defines.php'));
    constants['CLASSPATH'] = dirname(dirname('/data/sites/php/version2/configs/defines.php')) + '/classes/';
    constants['CORECLASSPATH'] = constants['CLASSPATH'];
    constants['MODULEPATH'] = dirname(dirname('/data/sites/php/version2/configs/defines.php')) + '/modules/';

    constants['GOOGLE_USER_AGENT'] = 'musicnation-gsa-crawler';
    constants['GOOGLE_SEARCH_URI'] = 'http://10.0.1.249/search';
    constants['SEARCH_DOMAIN'] = 'musicnation-search.com';
        

    constants['FORMIT_DYNARCH_CALENDAR_URI'] = '/js/jscalendar-1.0';
    constants['FORMIT_ZAPATEC_CALENDAR_URI'] = '/js/zapatec-calendar';

    constants['STATS_EVENT_USER_VIEW'] = 1;
    constants['STATS_EVENT_MEDIA_START'] = 2;
    constants['STATS_EVENT_MEDIA_STOP'] = 3;
    constants['STATS_EVENT_MEDIA_EMBED'] = 4;
    constants['STATS_EVENT_EPK_EMBED'] = 5;
    constants['STATS_EVENT_MP3_EMBED'] = 6;
    constants['STATS_EVENT_SHOWS_EMBED'] = 7;
    constants['STATS_EVENT_MEDIA_START_OFFSITE'] = 8;
    constants['STATS_EVENT_MEDIA_STOP_OFFSITE'] = 9;
    constants['STATS_EVENT_USEREMAILSIGNUP_EMBED'] = 10;
    constants['STATS_EVENT_BROADTEXTER_EMBED'] = 11;
    constants['STATS_EVENT_PLAYLIST_EMBED'] = 12;
    constants['STATS_EVENT_PROMOTER_EMBED'] = 13;
    constants['STATS_EVENT_PROMO_EMBED'] = 14;
    constants['STATS_EVENT_FEATURE_EMBED'] = 15;
    constants['STATS_EVENT_MEDIA_DOWNLOAD'] = 16;

    constants['UPDATE_GLOBAL'] = 1;
    constants['UPDATE_MEDIA'] = 2;
    constants['UPDATE_BLOG'] = 3;
    constants['UPDATE_EVENT'] = 4;
    constants['UPDATE_COMMENT'] = 5;
    constants['UPDATE_FAVORITE'] = 6;
    constants['UPDATE_VOTE'] = 7;
    constants['UPDATE_RATING'] = 8;
    constants['UPDATE_FRIEND'] = 9;
    constants['UPDATE_FORUMTHREAD'] = 10;
    constants['UPDATE_FORUMCOMMENT'] = 11;
    constants['UPDATE_ENTER_CONTEST'] = 12;
    constants['UPDATE_PROFILE'] = 13;
    constants['UPDATE_INFLUENCES'] = 14;
    constants['UPDATE_PHOTO'] = 15;
    constants['UPDATE_PLAYLISTADD'] = 16;

    constants['TIMESTAMP'] = gmdate('Ymdh');

    constants['DEFAULT_SNOCAP_ID'] = 'T3-31324-GS52N32YMF-G'; // Modern Society
    constants['ARTIST_PASSWORD'] = 'rock0n';

    constants['MAX_UPLOAD_FILESIZE'] = 104857600;
    constants['MAX_DAILY_BULLETIN_SENDS'] = 50;

    constants['FORMIT_TRANSLATE_FUNCTION'] = 'tr';

    //    print("M\n");
    //print("constants:" + tojson(constants) + '\n');
    apc_define_constants('global', constants); print( "LINE_250_" );
    //    print("N\n");


}

if (!defined('DOOV_BASE_PATH')) {
    doov_base_path =  dirname(dirname('/data/sites/php/version2/configs/defines.php')) + '/views';
}
viewpath =  doov_base_path;
