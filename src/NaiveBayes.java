
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class NaiveBayes {
   
    public static int examples  = 1000;
    public static int nooffeatures = 1899;
    public static int noofwords = 1899;
    public static int spammessages;
    public static int nonspammessages;
    public static int totaloccurences;
    public static int totaloccurencesspam;
    public static int totaloccurencesnonspam;
    
    public static int[] frequency;
    public static int[] frequency_spam;
    public static int[] frequency_nonspam;
    
    
    public static ArrayList<String> vocabulary = new ArrayList<String>();
    public static Hashtable<String, Integer> vocabmap = new Hashtable<String, Integer>();
    
    
    public static String read_file( String filename) throws FileNotFoundException, IOException{
        StringBuffer buffer = new StringBuffer();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while( ( line = br.readLine()) != null){
                buffer.append(line + " ");
        }
        br.close();
        return buffer.toString();
    }
    
    
    public static ArrayList<String> get_vocabList() throws FileNotFoundException, IOException{
        String vPath = "/Users/ykg2910/NetBeansProjects/TextClassification/datas/vocab.txt";
        ArrayList<String> vocabulary = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(vPath));
        String line;
        int count = 0;
        while( ( line = br.readLine()) != null){
                line = line.replaceAll("[0-9]+", "").trim();
                vocabulary.add(line);
                vocabmap.put(line, count++);  // extra
        }
        br.close();
        return vocabulary;
    }
    
 
    public static String preprocess( String email_content){
        email_content = email_content.toLowerCase();
        email_content = email_content.replaceAll("<[^<>]+>"," ");
        email_content = email_content.replaceAll("[0-9]+", "number");
        email_content = email_content.replaceAll("(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?", "httpaddr");
        email_content = email_content.replaceAll("[^\\s]+@[^\\s]+", "email_addr");
        email_content = email_content.replaceAll("[$]+", "dollar");
        email_content = email_content.replaceAll( "[\\p{Punct}]*", "");
        email_content = email_content.replaceAll("[^ a-zA-Z0-9]", "");
        email_content = email_content.trim();
        return email_content;
    }
    
    public static ArrayList<Integer> get_indices(String[] tokens, ArrayList<String> vocabulary){
        ArrayList<Integer> wordIndices = new ArrayList<Integer>();
        for( int i = 0 ; i < tokens.length; i++){
            if(tokens[i].length() < 3)
                continue;          
            if(vocabmap.containsKey(tokens[i])){
                wordIndices.add(vocabmap.get(tokens[i]));
            }
        }
        return wordIndices;
    }
    
    public static String[] stem_tokens(String[] tokens){
        Porter porter = new Porter();
        for( int i = 0; i < tokens.length; i++){
            if( tokens[i].length() < 3)
                continue;
            tokens[i] = porter.stripAffixes(tokens[i]);
        }
        return tokens;
    }
    
    public static int[] email_features(ArrayList<Integer> wordIndices){
            int[] x = new int[nooffeatures];
            for( int i = 0; i < wordIndices.size(); i++){
                x[wordIndices.get(i)] = 1;
            }
            return x;
    }
    
    public static int spam_messages(int[] target){
        int sum = 0;
        for( int i = 0; i < examples; i++){
            sum = sum + target[i];
        }
        return examples - sum;
    }
    
    public static void word_frequency(int[][] features , int[] target){
        
        frequency = new int[noofwords];
        frequency_spam = new int[noofwords];
        frequency_nonspam = new int[noofwords];
        
        for( int i = 0; i < examples; i++){
          if(target[i] == 1)
            for( int j = 0; j < noofwords; j++){
               frequency[j] = frequency[j] + features[i][j];
               frequency_spam[j] = frequency_spam[j] + features[i][j];
            }
          else
              for( int j = 0; j < noofwords; j++){
               frequency[j] = frequency[j] + features[i][j];
               frequency_nonspam[j] = frequency_nonspam[j] + features[i][j];
              }
        }
    }
    
    public static void totaloccurences( ){
        for(int i = 0; i < noofwords; i++){
            totaloccurences = totaloccurences + frequency[i];
            totaloccurencesspam = totaloccurencesspam + frequency_spam[i];
            totaloccurencesnonspam = totaloccurencesnonspam + frequency_nonspam[i];
        }
    }
    
    public static void training() throws FileNotFoundException, IOException{
        
        vocabulary = get_vocabList();
        String path = "/Users/ykg2910/NetBeansProjects/TextClassification/CSDMC2010_SPAM/TRAINING/";
        String[] messages = new String[examples];
        for( int i = 0 ; i < examples; i++){
            String name = "";
            if( i < 10){
                name = "TRAIN_0000" + i + ".eml";
            }
            else if( i < 100){
                name = "TRAIN_000" + i + ".eml";
            }
            else if( i < 1000){
                name = "TRAIN_00" + i + ".eml";
            }
            messages[i] = read_file(path + name);          
        }
        
        String[] processed = new String[examples];
        for( int i = 0; i < examples; i++){
            processed[i] = preprocess(messages[i]);
        }
        
        //System.out.println(processed[0]);
        
        String[][] tokens = new String[examples][];
        for( int i = 0; i  < examples; i++){
            tokens[i] = processed[i].split(" ");
        }
        
        for( int i = 0 ; i < examples; i++){
            tokens[i] = stem_tokens(tokens[i]);
        }
        
        // for( int i = 0; i < tokens[0].length; i++)   System.out.print(tokens[0][i] + " ");
        
        
        
        int[][] features = new int[examples][];
        ArrayList<Integer> wordIndices;
        for( int i = 0; i  < examples; i++){
            wordIndices = get_indices(tokens[i] , vocabulary );
            features[i] = email_features(wordIndices);
        }
        
        
        int[] target = new int[examples];
        BufferedReader br = new BufferedReader(new FileReader(path + "SPAMTrain.label"));
        String line;
        for( int i = 0; i < examples; i++){
            line = br.readLine();
            target[i] = Integer.parseInt(line.split(" ")[0]);
        }
        br.close();
        
        spammessages = spam_messages(target);
        nonspammessages = examples - spammessages;
                
        word_frequency(features, target);
        totaloccurences();
        
        
        System.out.println("total no of examples : " + examples);
        System.out.println("total no of words : " + noofwords);
        System.out.println("total no of spam examples : " + spammessages);
        System.out.println("total no of nonspam examples : " + nonspammessages);
        System.out.println("total no of occurences: " + totaloccurences);
        System.out.println("total no of occurences in spam : " + totaloccurencesspam);
        System.out.println("total no of occurences in nonspam : " + totaloccurencesnonspam);
        
    }
    
    public static int classify( String message){
        String[] tokens = message.split(" ");
        tokens = stem_tokens(tokens);
        ArrayList<Integer> wordIndices = get_indices(tokens , vocabulary );
        int[] features = email_features(wordIndices);
        Double probspam = Math.log((double)spammessages/examples);
        for( int i = 0; i < nooffeatures; i++){
            if(features[i] == 1){
                double t = Math.log((double)(frequency_spam[i] + 1)/( totaloccurencesspam + noofwords));
                probspam = probspam + t;
            }
        }
        Double probnonspam = Math.log((double)nonspammessages/examples);
        for( int i = 0; i < nooffeatures; i++){
            if(features[i] == 1){
                double t = Math.log((double)(frequency_nonspam[i] + 1)/( totaloccurencesnonspam + noofwords));
                probnonspam = probnonspam + t;
            }
        }
        
        //System.out.println(probspam + " " + probnonspam);
        if(probspam > probnonspam) return 1; return 0;
    }
    
    public static void main( String[] args) throws FileNotFoundException, IOException{
        
            training();
            int testexamples = 1000;
            String path = "/Users/ykg2910/NetBeansProjects/TextClassification/CSDMC2010_SPAM/TRAINING/";
            String[] messages = new String[testexamples];
        
            for( int i = 1000 ; i < 2000; i++){
                String name = "";
                 if( i < 10){
                     name = "TRAIN_0000" + i + ".eml";
                 }
                else if( i < 100){
                     name = "TRAIN_000" + i + ".eml";
                }
                else if( i < 1000){
                   name = "TRAIN_00" + i + ".eml";
                }
                else{
                    name = "TRAIN_0" + i + ".eml";
                }
            messages[i-1000] = read_file(path + name);          
          }
        
            
            String[] processed = new String[testexamples];
            for( int i = 0; i < testexamples; i++){
                  processed[i] = preprocess(messages[i]);
            }
            
            int[] target = new int[testexamples+examples];
            BufferedReader br = new BufferedReader(new FileReader(path + "SPAMTrain.label"));
            String line;
            for( int i = 0; i < testexamples+examples; i++){
                    line = br.readLine();
                    target[i] = Integer.parseInt(line.split(" ")[0]);
             }
             br.close();
            
            int count = 0;
            for( int i = 0; i < testexamples; i++){
               int result = classify(processed[i]); 
               if(result == target[examples+i]) count++;
            }
            System.out.println("correctly classified : " + count);
    }
    
}

class NewString {
  public String str;

  NewString() {
     str = "";
  }
}


class Porter {

  private String Clean( String str ) {
     int last = str.length();
     
     Character ch = new Character( str.charAt(0) );
     String temp = "";

     for ( int i=0; i < last; i++ ) {
         if ( ch.isLetterOrDigit( str.charAt(i) ) )
            temp += str.charAt(i);
     }
   
     return temp;
  } //clean
 
  private boolean hasSuffix( String word, String suffix, NewString stem ) {

     String tmp = "";

     if ( word.length() <= suffix.length() )
        return false;
     if (suffix.length() > 1) 
        if ( word.charAt( word.length()-2 ) != suffix.charAt( suffix.length()-2 ) )
           return false;
  
     stem.str = "";

     for ( int i=0; i<word.length()-suffix.length(); i++ )
         stem.str += word.charAt( i );
     tmp = stem.str;

     for ( int i=0; i<suffix.length(); i++ )
         tmp += suffix.charAt( i );

     if ( tmp.compareTo( word ) == 0 )
        return true;
     else
        return false;
  }

  private boolean vowel( char ch, char prev ) {
     switch ( ch ) {
        case 'a': case 'e': case 'i': case 'o': case 'u': 
          return true;
        case 'y': {

          switch ( prev ) {
            case 'a': case 'e': case 'i': case 'o': case 'u': 
              return false;

            default: 
              return true;
          }
        }
        
        default : 
          return false;
     }
  }

  private int measure( String stem ) {
    
    int i=0, count = 0;
    int length = stem.length();

    while ( i < length ) {
       for ( ; i < length ; i++ ) {
           if ( i > 0 ) {
              if ( vowel(stem.charAt(i),stem.charAt(i-1)) )
                 break;
           }
           else {  
              if ( vowel(stem.charAt(i),'a') )
                break; 
           }
       }

       for ( i++ ; i < length ; i++ ) {
           if ( i > 0 ) {
              if ( !vowel(stem.charAt(i),stem.charAt(i-1)) )
                  break;
              }
           else {  
              if ( !vowel(stem.charAt(i),'?') )
                 break;
           }
       } 
      if ( i < length ) {
         count++;
         i++;
      }
    } //while
    
    return(count);
  }

  private boolean containsVowel( String word ) {

     for (int i=0 ; i < word.length(); i++ )
         if ( i > 0 ) {
            if ( vowel(word.charAt(i),word.charAt(i-1)) )
               return true;
         }
         else {  
            if ( vowel(word.charAt(0),'a') )
               return true;
         }
        
     return false;
  }

  private boolean cvc( String str ) {
     int length=str.length();

     if ( length < 3 )
        return false;
    
     if ( (!vowel(str.charAt(length-1),str.charAt(length-2)) )
        && (str.charAt(length-1) != 'w') && (str.charAt(length-1) != 'x') && (str.charAt(length-1) != 'y')
        && (vowel(str.charAt(length-2),str.charAt(length-3))) ) {

        if (length == 3) {
           if (!vowel(str.charAt(0),'?')) 
              return true;
           else
              return false;
        }
        else {
           if (!vowel(str.charAt(length-3),str.charAt(length-4)) ) 
              return true; 
           else
              return false;
        } 
     }   
  
     return false;
  }

  private String step1( String str ) {
 
     NewString stem = new NewString();

     if ( str.charAt( str.length()-1 ) == 's' ) {
        if ( (hasSuffix( str, "sses", stem )) || (hasSuffix( str, "ies", stem)) ){
           String tmp = "";
           for (int i=0; i<str.length()-2; i++)
               tmp += str.charAt(i);
           str = tmp;
        }
        else {
           if ( ( str.length() == 1 ) && ( str.charAt(str.length()-1) == 's' ) ) {
              str = "";
              return str;
           }
           if ( str.charAt( str.length()-2 ) != 's' ) {
              String tmp = "";
              for (int i=0; i<str.length()-1; i++)
                  tmp += str.charAt(i);
              str = tmp;
           }
        }  
     }

     if ( hasSuffix( str,"eed",stem ) ) {
           if ( measure( stem.str ) > 0 ) {
              String tmp = "";
              for (int i=0; i<str.length()-1; i++)
                  tmp += str.charAt( i );
              str = tmp;
           }
     }
     else {  
        if (  (hasSuffix( str,"ed",stem )) || (hasSuffix( str,"ing",stem )) ) { 
           if (containsVowel( stem.str ))  {

              String tmp = "";
              for ( int i = 0; i < stem.str.length(); i++)
                  tmp += str.charAt( i );
              str = tmp;
              if ( str.length() == 1 )
                 return str;

              if ( ( hasSuffix( str,"at",stem) ) || ( hasSuffix( str,"bl",stem ) ) || ( hasSuffix( str,"iz",stem) ) ) {
                 str += "e";
           
              }
              else {   
                 int length = str.length(); 
                 if ( (str.charAt(length-1) == str.charAt(length-2)) 
                    && (str.charAt(length-1) != 'l') && (str.charAt(length-1) != 's') && (str.charAt(length-1) != 'z') ) {
                     
                    tmp = "";
                    for (int i=0; i<str.length()-1; i++)
                        tmp += str.charAt(i);
                    str = tmp;
                 }
                 else
                    if ( measure( str ) == 1 ) {
                       if ( cvc(str) ) 
                          str += "e";
                    }
              }
           }
        }
     }

     if ( hasSuffix(str,"y",stem) ) 
        if ( containsVowel( stem.str ) ) {
           String tmp = "";
           for (int i=0; i<str.length()-1; i++ )
               tmp += str.charAt(i);
           str = tmp + "i";
        }
     return str;  
  }

  private String step2( String str ) {

     String[][] suffixes = { { "ational", "ate" },
                                    { "tional",  "tion" },
                                    { "enci",    "ence" },
                                    { "anci",    "ance" },
                                    { "izer",    "ize" },
                                    { "iser",    "ize" },
                                    { "abli",    "able" },
                                    { "alli",    "al" },
                                    { "entli",   "ent" },
                                    { "eli",     "e" },
                                    { "ousli",   "ous" },
                                    { "ization", "ize" },
                                    { "isation", "ize" },
                                    { "ation",   "ate" },
                                    { "ator",    "ate" },
                                    { "alism",   "al" },
                                    { "iveness", "ive" },
                                    { "fulness", "ful" },
                                    { "ousness", "ous" },
                                    { "aliti",   "al" },
                                    { "iviti",   "ive" },
                                    { "biliti",  "ble" }};
     NewString stem = new NewString();

     
     for ( int index = 0 ; index < suffixes.length; index++ ) {
         if ( hasSuffix ( str, suffixes[index][0], stem ) ) {
            if ( measure ( stem.str ) > 0 ) {
               str = stem.str + suffixes[index][1];
               return str;
            }
         }
     }

     return str;
  }

  private String step3( String str ) {

        String[][] suffixes = { { "icate", "ic" },
                                       { "ative", "" },
                                       { "alize", "al" },
                                       { "alise", "al" },
                                       { "iciti", "ic" },
                                       { "ical",  "ic" },
                                       { "ful",   "" },
                                       { "ness",  "" }};
        NewString stem = new NewString();

        for ( int index = 0 ; index<suffixes.length; index++ ) {
            if ( hasSuffix ( str, suffixes[index][0], stem ))
               if ( measure ( stem.str ) > 0 ) {
                  str = stem.str + suffixes[index][1];
                  return str;
               }
        }
        return str;
  }

  private String step4( String str ) {
        
     String[] suffixes = { "al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "sion", "tion",
                           "ou", "ism", "ate", "iti", "ous", "ive", "ize", "ise"};
     
     NewString stem = new NewString();
        
     for ( int index = 0 ; index<suffixes.length; index++ ) {
         if ( hasSuffix ( str, suffixes[index], stem ) ) {
           
            if ( measure ( stem.str ) > 1 ) {
               str = stem.str;
               return str;
            }
         }
     }
     return str;
  }

  private String step5( String str ) {

     if ( str.charAt(str.length()-1) == 'e' ) { 
        if ( measure(str) > 1 ) {/* measure(str)==measure(stem) if ends in vowel */
           String tmp = "";
           for ( int i=0; i<str.length()-1; i++ ) 
               tmp += str.charAt( i );
           str = tmp;
        }
        else
           if ( measure(str) == 1 ) {
              String stem = "";
              for ( int i=0; i<str.length()-1; i++ ) 
                  stem += str.charAt( i );

              if ( !cvc(stem) )
                 str = stem;
           }
     }
     
     if ( str.length() == 1 )
        return str;
     if ( (str.charAt(str.length()-1) == 'l') && (str.charAt(str.length()-2) == 'l') && (measure(str) > 1) )
        if ( measure(str) > 1 ) {/* measure(str)==measure(stem) if ends in vowel */
           String tmp = "";
           for ( int i=0; i<str.length()-1; i++ ) 
               tmp += str.charAt( i );
           str = tmp;
        } 
     return str;
  }

  private String stripPrefixes ( String str) {

     String[] prefixes = { "kilo", "micro", "milli", "intra", "ultra", "mega", "nano", "pico", "pseudo"};

     int last = prefixes.length;
     for ( int i=0 ; i<last; i++ ) {
         if ( str.startsWith( prefixes[i] ) ) {
            String temp = "";
            for ( int j=0 ; j< str.length()-prefixes[i].length(); j++ )
                temp += str.charAt( j+prefixes[i].length() );
            return temp;
         }
     }
     
     return str;
  }


  private String stripSuffixes( String str ) {

     str = step1( str );
     if ( str.length() >= 1 )
        str = step2( str );
     if ( str.length() >= 1 )
        str = step3( str );
     if ( str.length() >= 1 )
        str = step4( str );
     if ( str.length() >= 1 )
        str = step5( str );
 
     return str; 
  }


  public String stripAffixes( String str ) {

    str = str.toLowerCase();
    str = Clean(str);
  
    if (( str != "" ) && (str.length() > 2)) {
       str = stripPrefixes(str);

       if (str != "" ) 
          str = stripSuffixes(str);

    }   

    return str;
    } //stripAffixes

} //class
