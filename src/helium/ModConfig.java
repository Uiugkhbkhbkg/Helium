package helium;

import arc.files.Fi;
import arc.util.Log;
import arc.util.serialization.Jval;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ModConfig {
  private static final int configVersion = 0;
  private static final Field[] configs = ModConfig.class.getFields();

  private String lastContext;

  private final Fi configFile;
  private final Fi configBack;
  private final Fi internalConfigFile;

  @Order(0) public boolean loadInfo;
  @Order(1) public boolean enableBlur;
  @Order(2) public int blurLevel;
  @Order(3) public float backBlurLen;

  public ModConfig(Fi configDir, Fi internalSource){
    configFile = configDir.child("mod_config.hjson");
    configBack = configDir.child("mod_config.hjson.bak");
    internalConfigFile = internalSource;
  }

  public void load(){
    if(!configFile.exists()){
      internalConfigFile.copyTo(configFile);
      Log.info("Configuration file is not exist, copying the default configuration");
      load(configFile);
    }
    else{
      if(!load(configFile)){
        Fi backup;
        configFile.copyTo(backup = configBack);
        internalConfigFile.copyTo(configFile);
        Log.info("default configuration file version updated, eld config should be override(backup file for old file was created)");
        load(configFile);
        String tmp = lastContext;
        load(backup, true);
        lastContext = tmp;

        save();
      }
    }

    if(loadInfo) printConfig();
  }

  public void printConfig(){
    StringBuilder results = new StringBuilder();

    for(Field cfg: configs){
      try{
        results.append("  ").append(cfg.getName()).append(" = ").append(cfg.get(this)).append(";").append(System.lineSeparator());
      }catch(IllegalAccessException e){
        throw new RuntimeException(e);
      }
    }

    Log.info("Mod config loaded! The config data:[" + System.lineSeparator() + results + "]");
  }

  public boolean load(Fi file){
    return load(file, false);
  }

  public boolean load(Fi file, boolean loadOld){
    int n;
    char[] part = new char[8192];
    StringBuilder sb = new StringBuilder();
    try(Reader r = file.reader()){
      while((n = r.read(part, 0, part.length)) != -1){
        sb.append(part, 0, n);
      }
    }catch(IOException e){
      throw new RuntimeException(e);
    }

    lastContext = sb.toString();
    Jval config = Jval.read(lastContext);

    boolean old = config.get("configVersion").asInt() != configVersion;

    if(!loadOld && old) return false;

    for(Field cfg: configs){
      if(!config.has(cfg.getName())) continue;

      String temp = config.get(cfg.getName()).toString();
      try{
        cfg.set(this, warp(cfg.getType(), temp));
      }
      catch(IllegalArgumentException | IllegalAccessException e){
        Log.err(e);
      }
    }

    return !old;
  }

  public void save(){
    try{
      save(configFile);
    }catch(IOException e){
      Log.err(e);
    }
  }

  @SuppressWarnings({"HardcodedFileSeparator", "unchecked"})
  public void save(Fi file) throws IOException{
    Jval tree = Jval.newObject();

    Jval.JsonMap map = tree.asObject();
    map.put("configVersion", Jval.valueOf(configVersion));

    Field[] configs = ModConfig.class.getFields();
    Arrays.sort(configs, (f1, f2) -> {
      float f = f1.getAnnotation(Order.class).value() - f2.getAnnotation(Order.class).value();
      if(f == 0) return 0;
      return f > 0? 1: -1;
    });
    try{
      for(Field cfg: configs){
        String key = cfg.getName();
        Object obj = cfg.get(this);
        if(obj == null){
          if(CharSequence.class.isAssignableFrom(cfg.getType())){
            map.put(key, Jval.valueOf(""));
          }
          else if(cfg.getType().isArray()){
            map.put(key, Jval.newArray());
          }
          else if(cfg.getType().isEnum()){
            map.put(key, Jval.valueOf(firstEnum((Class<? extends Enum<?>>) cfg.getType()).name()));
          }
        }
        else map.put(key, pack(obj));
      }
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }

    StringWriter writer = new StringWriter();
    tree.writeTo(writer, Jval.Jformat.formatted);

    String str = writer.getBuffer().toString();
    BufferedReader r1 = new BufferedReader(new StringReader(str));
    BufferedReader r2 = new BufferedReader(new StringReader(lastContext));

    BufferedWriter write = new BufferedWriter(file.writer(false));
    
    String line;
    while((line = r2.readLine()) != null){
      int i;
      String after = "";
      if((i = line.indexOf("//")) != -1){
        if(line.substring(0, i).trim().isEmpty()){
          write.write(line);
        }
        else after = line.substring(i);
      }
      else if(line.isEmpty()){
        write.write("");
      }

      if(!line.isEmpty() && (i == -1 || !after.equals(""))) write.write(r1.readLine());

      write.write(after);
      
      write.write(System.lineSeparator());
      write.flush();
    }
    write.close();
    r1.close();
    r2.close();
  }

  private static Jval pack(Object value){
    Class<?> type = value.getClass();
    if(type == Integer.class) return Jval.valueOf((int)value);
    else if(type == Byte.class) return Jval.valueOf((byte)value);
    else if(type == Short.class) return Jval.valueOf((short)value);
    else if(type == Boolean.class) return Jval.valueOf((boolean)value);
    else if(type == Long.class) return Jval.valueOf((long)value);
    else if(type == Character.class) return Jval.valueOf((char)value);
    else if(type == Float.class) return Jval.valueOf((float)value);
    else if(type == Double.class) return Jval.valueOf((double)value);
    else if(CharSequence.class.isAssignableFrom(type)) return Jval.valueOf((String) value);
    else if(type.isArray()) return packArray(value);
    else if(type.isEnum()) return Jval.valueOf(((Enum<?>)value).name());
    else throw new RuntimeException("invalid type: " + type);
  }

  private static Jval packArray(Object array){
    if(!array.getClass().isArray()) throw new RuntimeException("given object was not an array");

    int len = Array.getLength(array);
    Jval res = Jval.newArray();
    Jval.JsonArray arr = res.asArray();
    for(int i = 0; i < len; i++){
      arr.add(pack(Array.get(array, i)));
    }

    return res;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> T warp(Class<T> type, String value){
    if(type == int.class) return (T)Integer.valueOf(value);
    else if(type == byte.class) return (T)Byte.valueOf(value);
    else if(type == short.class) return (T)Short.valueOf(value);
    else if(type == boolean.class) return (T)Boolean.valueOf(value);
    else if(type == long.class) return (T)Long.valueOf(value);
    else if(type == char.class) return (T)Character.valueOf(value.charAt(0));
    else if(type == float.class) return (T)Float.valueOf(value);
    else if(type == double.class) return (T)Double.valueOf(value);
    else if(CharSequence.class.isAssignableFrom(type)) return (T) value;
    else if(type.isArray()) return toArray(type, value);
    else if(type.isEnum()) return (T) Enum.valueOf((Class) type, value);
    else throw new RuntimeException("invalid type: " + type);
  }

  @SuppressWarnings("unchecked")
  private static <T> T toArray(Class<T> type, String value){
    if(!type.isArray()) throw new RuntimeException("class " + type + " was not an array");
    Jval.JsonArray a = Jval.read(value).asArray();
    Class<?> eleType = type.getComponentType();
    Object res = Array.newInstance(eleType, a.size);
    for(int i = 0; i < a.size; i++){
      Array.set(res, i, warp(eleType, a.get(i).toString()));
    }

    return (T) res;
  }

  @SuppressWarnings("unchecked")
  private static <T> T firstEnum(Class<T> type){
    if(!type.isEnum()) throw new RuntimeException("class " + type + " was not an enum");
    try {
      return ((T[]) type.getMethod("values").invoke(null))[0];
    } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public void reset() {
    configFile.copyTo(configBack);
    configFile.delete();

    Log.info("[Singularity][INFO] mod config has been reset, old config file saved to file named \"mod_config.hjson.bak\"");
    load();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  private @interface Order{
    float value();
  }
}
