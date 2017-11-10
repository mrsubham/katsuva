package katsuva;
import java.io.*;
import java.util.*;


class Identifier{
   int value;
   int scope;
   String type; // if needed
   
   Identifier()
   {
       value=-999;
       scope=0;
       type=null;
   }
}

public class Katsuva {
    
    static String[] input;
    static int line_number;
    static Set<String> pre_defined = new HashSet<String>();
    static Map<Integer,Integer> from_endfrom = new HashMap<Integer,Integer>(); // to store line numbers start of scope to end of scope of loops
    static Map<Integer,Integer> if_endif = new HashMap<Integer,Integer>();   // to store start and end of scope of 
    static Map<Integer,Integer> continue_ = new HashMap<Integer,Integer>();
    static Map<Integer,Integer> break_ = new HashMap<Integer,Integer>();
    static Map<String,Identifier> user_variables = new HashMap<String,Identifier>();
    static Stack from_=new Stack();
    static Stack if_=new Stack();
     static Stack label_=new Stack();
    static int scope=0;
    static int flag_error=0;
    
    public static void main(String[] args) throws Exception {
        initialize();
         File file = new File("..\\katsuva\\src\\katsuva\\error.txt");
            BufferedWriter error = new BufferedWriter(new FileWriter(file));
        String base = "..\\katsuva\\src\\katsuva\\";
        String fileName = "input.txt"; // to be changed to command line input from user
        fileName = base + fileName;
        line_number=-1;
        Scanner sc = new Scanner(new File(fileName));
       
        while (sc.hasNextLine()) {
          Analysis(sc.nextLine(),error);
        }
        if(!(from_.isEmpty())) 
        {
            // from without end_from
            flag_error=1;
            
            error.write("End_from missing for from at line no."+(int)from_.peek()+'\n');
        }
        if(!(if_.isEmpty()))
            {
           // if without end_if
            flag_error=1;
            
            error.write("End_if missing for from at line no."+(int)if_.peek()+'\n');
        }
        sc.close();
        error.close();
        if(flag_error==1)
        {
            System.out.println("Error in code");
             Scanner s = new Scanner(new File("..\\katsuva\\src\\katsuva\\error.txt"));
               while (s.hasNextLine())
               {
                   System.out.println("\u001B[31m"+s.nextLine()+ "\u001B[0m");
                   
               }
            s.close();
            System.exit(0);
        }
        
        String result = base+"output.txt";
         File file2 = new File(result);
            BufferedWriter output = new BufferedWriter(new FileWriter(file2));
          
           Synthesis(output);
        System.out.println(user_variables.toString());
        System.out.println(from_endfrom.toString());
        System.out.println(if_endif.toString());
        System.out.println(continue_.toString());
        System.out.println(break_.toString());
     
    }
    public static void initialize()   // initializing the pre defined variables
    {
        pre_defined.add("is");
        pre_defined.add("from");
        pre_defined.add("if");
        pre_defined.add("end_if");
        pre_defined.add("end_from");
        pre_defined.add("continue");
        pre_defined.add("break");
        pre_defined.add("to");
        pre_defined.add("inc_by");
        pre_defined.add("write");
        pre_defined.add("else");
        pre_defined.add("in");    
    }
    public static void Analysis(String line,BufferedWriter error) throws Exception
    {
        line_number++;
        if(line.equals(""))
            return;
          String[] lexeme=line.split(" ");
          lexeme[0]=lexeme[0].toString();
          if(lexeme[0].equals("if"))
          {
              scope++;
              if_.push(line_number);
          }
          else if(lexeme[0].equals("else"))
          {
               try{
                  int a=(int)from_.peek();
               }
               catch(Exception e)
               {
                   flag_error=1;
                   error.write("Else without an if at line no."+line_number+'\n');
              }
              
          }
          else if(lexeme[0].equals("from"))
          {
              scope++;
              from_.push(line_number);
              try{Integer.parseInt(lexeme[1]);}
              catch(Exception e)
              {
                 if(!pre_defined.contains(lexeme[1]))  
                  {                  
                     Identifier temp=new Identifier();
                     temp.scope=scope;
                     
                     user_variables.put(lexeme[1],temp);
                 }
             }    
              if(!line.contains("to"))
              {
                  flag_error=1;
                  error.write("Range not specified in from statement");
              }
            
          }
          else if(lexeme[0].equals("end_if"))
          {
              scope--;
              try{if_endif.put((int)if_.pop(),line_number);}
              catch(Exception e)
              {
                  //end_if without if
                  flag_error=1;
            
            error.write("Extra End_if placed at line no."+line_number+'\n');
              }
          }
          else if(lexeme[0].equals("end_from"))
          {
              scope--;
              try{from_endfrom.put((int)from_.pop(),line_number);}
              catch(Exception e)
              {
                 // end_from without from  
                  flag_error=1;
            
            error.write("Extra End_from placed at line no."+line_number+'\n');
              } 
          }
         else if(lexeme[0].equals("continue"))
          {
              try{
                  continue_.put(line_number,(int)from_.peek());
              }
              catch(Exception e)
              {
                 //if continue occurs after end_from 
                  flag_error=1;
            
            error.write("Extra continue placed at line no."+line_number+'\n');
              }
              return;
          }
         else if(lexeme[0].equals("break"))
          {
              try{break_.put(line_number,(int)from_.peek());} // this needs to be changed
              catch(Exception e)
              {
                 //if break occurs after end_from 
                   flag_error=1;
            
            error.write("Break can only be placed within from block ; Extra break placed at line no."+line_number +'\n');
              }
              return;
          }
         
          int i;
          for(i=0;i<lexeme.length;i++)
          {
              int flag=0;
              String lex,one="",two="";
              if(lexeme[i].contains(">"))
              {
                  int loc=lexeme[i].indexOf('>');
                  two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                  flag=1;
              }
              if(lexeme[i].contains("<"))
              {
                  int loc=lexeme[i].indexOf('<');
                  two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                  flag=1;
              }
              if(lexeme[i].contains("=="))
              {
                  int loc=lexeme[i].indexOf('=');
                  two=lexeme[i].substring(loc+2);
                  one=lexeme[i].substring(0,loc);
                  flag=1;
              }
              if(lexeme[i].contains(","))
              {
                  int loc=lexeme[i].indexOf(',');
                  two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                  flag=1;
              }
              if(flag==1)
              {
               
              try{Integer.parseInt(one);}
              catch(Exception e)
              {
                 if(!pre_defined.contains(one) && one.charAt(0)!='"')  
                  {                  
                     Identifier temp=new Identifier();
                     temp.scope=scope;                
                     user_variables.put(one,temp);
                 }
             }
              try{Integer.parseInt(two);}
              catch(Exception e)
              {
                 if(!pre_defined.contains(two)  && two.charAt(0)!='"')  
                  {                  
                     Identifier temp=new Identifier();
                     temp.scope=scope;                
                     user_variables.put(two,temp);
                 }
             }    
                  
           }    
              if(flag==0)
              {
              try{Integer.parseInt(lexeme[i]);}
              catch(Exception e)
              {
                 if(!pre_defined.contains(lexeme[i]))  
                  {                  
                     Identifier temp=new Identifier();
                     temp.scope=scope;                
                     user_variables.put(lexeme[i],temp);
                 }
             }    
          }
          }
       // System.out.println(line);  
    }  
    
     public static void Synthesis(BufferedWriter output) throws Exception
    {
         String base = "..\\katsuva\\src\\katsuva\\";
        String fileName = "input.txt"; // to be changed to command line input from user
        fileName = base + fileName;
        int label_count=0;
        String for_label="";
        Scanner sc = new Scanner(new File(fileName));
        while(sc.hasNextLine())
        {
            String line=sc.nextLine();
            
            
             if(line.equals(""))
                 continue;
          String[] lexeme=line.split(" ");
           lexeme[0]=lexeme[0].toString();
           Identifier temp1,temp2;
           int value1=0,value2=0,value3=0;
           int inside_if=0;
          if(lexeme[0].equals("if"))
          {
              inside_if=1;
               String lex,one="",two="";
               int i=1;
              if(lexeme[i].contains(">"))
              {
                  int loc=lexeme[i].indexOf('>');
                  two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                   temp1=user_variables.get(one);
                   value1=temp1.value;
                   temp2=user_variables.get(two);
                   value2=temp2.value;
                   if(value1>value2)
                       value3=1;
                   else
                       value3=0;
                   String label="LABEL"+(++label_count);
                   label_.push(label);
                    output.write("MOV R1,"+value1+"\n"+"MOV R2,"+value2+"\n"+"CMP R1,R2"+"\n"+"JL "+label+"\n");
            
                  
              }
              if(lexeme[i].contains("<"))
              {
                  int loc=lexeme[i].indexOf('<');
                  two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                   two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                   temp1=user_variables.get(one);
                   value1=temp1.value;
                   temp2=user_variables.get(two);
                   value2=temp2.value;
                   if(value1<value2)
                       value3=1;
                   else
                       value3=0;
                    String label="LABEL"+(++label_count);
                   label_.push(label);
                 output.write("MOV R1,"+value1+"\n"+"MOV R2,"+value2+"\n"+"CMP R1,R2"+"\n"+"JG  "+label+"\n");              }
              if(lexeme[i].contains("=="))
              {
                  int loc=lexeme[i].indexOf('=');
                  two=lexeme[i].substring(loc+2);
                  one=lexeme[i].substring(0,loc);
                   two=lexeme[i].substring(loc+1);
                  one=lexeme[i].substring(0,loc);
                   temp1=user_variables.get(one);
                   value1=temp1.value;
                   temp2=user_variables.get(two);
                   value2=temp2.value;
                   if(value1==value2)
                       value3=1;
                   else
                       value3=0;
                   output.write("MOV R1,"+value1+"\n"+"MOV R2,"+value2+"\n"+"CMP R1,R2"+"\n"+"JZ LABEL"+(++label_count)+"\n");
              }
              
           
          }
           else if(lexeme[0].equals("end_if"))
          {
              /*if(inside_if==0)
              { output.write("LABEL"+(label_count+2)+':'+"\n");}*/
              String label=label_.pop().toString();
              output.write(label+':'+"\n");
        }
            
            else if(lexeme[0].equals("from"))
          {
              int start=Integer.parseInt(lexeme[3]);
              int end=Integer.parseInt(lexeme[5]);
              int inc=1;
              try{
               inc=Integer.parseInt(lexeme[7]);
              }
              catch(Exception e)
              {
                  inc=1;
              }
              if(start>end)
                  inc=inc*-1;
               temp1=user_variables.get(lexeme[1]);
               temp1.value=Integer.parseInt(lexeme[3]);
               output.write("MOV R1,"+(temp1.value)+"\n");
                temp1=user_variables.get(lexeme[1]);
               temp1.value=Integer.parseInt(lexeme[5]);
               output.write("MOV R2,"+(temp1.value)+"\n");
               output.write("MOV CX,"+(Math.abs(start-end))+"\n");
                String label="LABEL"+(++label_count);
                ++label_count;
                for_label=label;
               output.write(label+':'+"\n");
               output.write("CMP R1,R2 \n");
               output.write("JZ "+(for_label+1)+"\n");
               output.write("INC R1 \n");
               
              
        }
            else if(lexeme[0].equals("continue"))
          {
              output.write("JMP "+for_label+"\n");
             
        }
             else if(lexeme[0].equals("break"))
          {
              output.write("JMP "+(for_label+1)+"\n");
             
        }
              else if(lexeme[0].equals("end_from"))
          {
              output.write((for_label+1)+':'+"\n");
             
        }
               else if(lexeme[0].equals("else"))
          {
              inside_if=0;
               
                String label="LABEL"+(++label_count);
                   label_.push(label);
                 output.write("JMP "+label+':'+"\n");
               label=label_.pop().toString();
              output.write(label+':'+"\n");
              
             
        }
                else if(lexeme[0].equals("write"))
          {
              if(lexeme[1].contains(","))
              {
                  int loc=lexeme[1].indexOf(',');
                  String two=lexeme[1].substring(loc+1);
                  String one=lexeme[1].substring(0,loc);
                  
                   if(!one.contains("\""))
                  {
                      temp1=user_variables.get(one);
                       one="\""+temp1.value+"\"";
                  }
                   output.write("MOV DX,"+one+"\n");
                      output.write("MOV CX,"+one.length()+"\n");
                      output.write("MOV AH,09H \n");
                      output.write("INT 21H \n");
                  
                  if(!two.contains("\""))
                  {
                      temp1=user_variables.get(two);
                       two="\""+temp1.value+"\"";
                     
                  }
                   output.write("MOV DX,"+two+"\n");
                      output.write("MOV CX,"+two.length()+"\n");
                      output.write("MOV AH,09H \n");
                      output.write("INT 21H \n");
              }
           
              
          
          }
               
         else if(lexeme[1].equals("is"))
          {
               temp1=user_variables.get(lexeme[0]);
               int temp=0;
               try{
                   
                   Identifier temp3= user_variables.get(lexeme[2]);
                   temp=temp3.value;
                  
               }
               catch(Exception e)
               {
                   temp=Integer.parseInt(lexeme[2]);
               }
               temp1.value=temp;
               
                   
          }
         
          
            
        }
        
        
      output.close();  
    }
    }

