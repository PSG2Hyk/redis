package com.hyk;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class App
{

    public Jedis connectredis()
    {
        Jedis jedis = new Jedis("localhost");
        jedis.auth("123456");
        return jedis;
    }

    public static void main(String[] args) {

    }

    public PageInfo<String> getpagieValue(String hashname, int pagenumber, int pagesize)
    {
        Map<String,String> usermap=connectredis().hgetAll(hashname);
        List list=new ArrayList();
        for (String key : usermap.keySet())
        {
            list.add("key= " + key + " and value= " + usermap.get(key));
        }
        PageInfo<String> pageInfo=new PageInfo<String>(list,pagesize);
        return pageInfo;
    }


    public void saveHashValue(Map<String,String> map)
    {
        connectredis().hmset("mymap",map);
    }

    public Map<String, String> getAllValue(String hashname)
    {
        Map<String,String> usermap=connectredis().hgetAll(hashname);
        return usermap;
    }

    public void delHashValue(String hashname,String valuename)
    {
        connectredis().hdel(hashname,valuename);
    }

    public List<String> updateContent(String hashname,String valuename,String value)
    {
        connectredis().hset(hashname,valuename,value);
        List<String> stringList=connectredis().hmget(hashname,valuename);
        return stringList;
    }
    public void test1(){

        connectredis().hset("books","java","think in java");
        connectredis().lpush("class","math","English");
        connectredis().sadd("成绩","数学","英语");
        connectredis().zadd("english:scoreboard",90,"张三");
        connectredis().zadd("english:scoreboard",91,"李四");
        connectredis().zadd("english:scoreboard",92,"王多多");
        String name = connectredis().get("name");
        connectredis().close();
    }

    public void test2(){
        User user= new User();
        user.setName("com");
        user.setSex("male");
        String userStr = JSON.toJSONString(user);
        connectredis().set("user",userStr);
        String user1 = connectredis().get("user");
        System.out.println(user1);
        User user2 = JSON.parseObject(user1, User.class);
        System.out.println(user2);
        connectredis().close();
    }

    public void save(){
        Post post = new Post();
        post.setAuthor("Author");
        post.setContent("ABC");
        post.setTitle("博客Title");
        Long postId = SavePost(post,connectredis());
        GetPost(postId,connectredis());
        Post post1 = updateTitle(postId, connectredis());
        System.out.println(post1);
        deleteBlog(postId,connectredis());
        connectredis().close();
    }

    public Long SavePost(Post post,Jedis jedis){
        Long postId = jedis.incr("posts");
        String myPost = JSON.toJSONString(post);
        jedis.set("post:"+postId+":data",myPost);
        return postId;
    }


    public Post GetPost(Long postId,Jedis jedis){
        String getPost = jedis.get("post:" + postId + ":data");
        jedis.incr("post:" + postId + ":page.view");
        Post parseObject = JSON.parseObject(getPost, Post.class);
        System.out.println("这是第"+postId+"篇文章"+parseObject);
        return parseObject;
    }

    public Post updateTitle(Long postId,Jedis jedis){
        Post post = GetPost(postId, jedis);
        post.setTitle("更改后的标题");
        String myPost = JSON.toJSONString(post);
        jedis.set("post:"+postId+":data",myPost);
        System.out.println("修改完成");
        return post;
    }

    public void deleteBlog(Long postId,Jedis jedis){
        jedis.del("post:" + postId + ":data");
        jedis.del("post:"+postId+":page.view");
        System.out.println("删除成功");
    }

}
