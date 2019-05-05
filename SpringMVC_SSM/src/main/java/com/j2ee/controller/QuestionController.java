package com.j2ee.controller;

import com.auth0.jwt.interfaces.Claim;
import com.j2ee.mapper.QuestionMapper;
import com.j2ee.po.Question;
import com.j2ee.util.JwtUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/question")
public class QuestionController {
    @RequestMapping(value = "/questionList",method = RequestMethod.POST)
    @ResponseBody
        public List<Question> questionList(@RequestBody(required=true) Map<String,Object> map){
        ApplicationContext applicationContext=new ClassPathXmlApplicationContext("applicationContext.xml");
        QuestionMapper questionMapper=applicationContext.getBean(QuestionMapper.class);
        int p= (int) map.get("p");
        int num= (int) map.get("num");
        int offSet=(p-1)*num;
        List<Question> question = questionMapper.findAllQuestion(offSet,num);
        for(int i=0;i<question.size();i++){
            Question question1=question.get(i);
            String detail=question1.getDetail();
            int x=detail.length();
            if(x>200){
                x=200;
            }
            String smallDetail=detail.substring(0,x);
            question1.setDetail(smallDetail);
        }
        return question;
    }
    @RequestMapping(value = "/questionDetail",method = RequestMethod.POST)
    @ResponseBody
    public Question questionDetail(@RequestBody Question question){
        ApplicationContext applicationContext=new ClassPathXmlApplicationContext("applicationContext.xml");
        QuestionMapper questionMapper=applicationContext.getBean(QuestionMapper.class);
        Question question1=questionMapper.findQuestionWithAnswers(question.getQuestionID());   //没有回答时 无结果
        return question1;
    }
    @RequestMapping(value = "/addQuestion",method = RequestMethod.POST)
    @ResponseBody
    public Map addQuestion(@RequestBody(required=true) Map<String,Object> map) throws ParseException {
        ApplicationContext applicationContext=new ClassPathXmlApplicationContext("applicationContext.xml");
        QuestionMapper questionMapper=applicationContext.getBean(QuestionMapper.class);
        String token = (String) map.get("token");
        Map<String, Claim> a = JwtUtil.verifyToken(token);
        String userID = JwtUtil.getAppUID(token);
        Question question1=new Question();
        question1.setUserID(userID);
        String question= (String) map.get("question");
        question1.setQuestion(question);
        String detail= (String) map.get("detail");
        question1.setDetail(detail);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        long time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime();
        int changeTime = (int) (time / 1000);
        question1.setCreateTime(changeTime);
        int result=questionMapper.addQuestion(question1);
        int questionID=questionMapper.findLargestQuestionID();
        Map<String,Object> map1=new HashMap<String ,Object>();
        if(result>0){
            map1.put("code",0);
            map1.put("questionID",questionID);
        }

        else{
            map1.put("code",-1);
        }
        return map1;
    }
}