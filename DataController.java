package com.myhome.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.myhome.dto.DataDto;
import com.myhome.dto.DefaultDto;
import com.myhome.service.DataService;

@Controller
public class DataController {
	
	@Autowired
	DataService dataService;
	
	//클래스(전역)변수
	public static String path = "C:/spring-workspace/myhome/src/main/webapp/data";
	
	
	/**
	 * 데이터 입력
	 * @return
	 */
	@GetMapping("dataWrite")
	public String dataWrite() {
		
		return "board/dataWrite";
	}
	
	
	
	/**
	 * 저장처리(파일 카피~~)
	 * @param request
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	@PostMapping("dataBoardInsert")
	@ResponseBody
	public String insertDataBoard(MultipartHttpServletRequest request
								 ,DataDto dto)       throws Exception {
		
		String message = "ok";
		
		//넘어온 데이터를 가져옴
		Map map = request.getFileMap();
		
		//Map의 키 값(들)을 가져옴
		Iterator it = map.entrySet().iterator();
		
		String filenames = "";
		
		int cnt = 0;
		
		while(it.hasNext()) {	//파일의 존재유무
			//키 값이 있는 위치로 커서를 내려보냄
			Entry entry = (Entry) it.next();
			
			//해당 위치에서 파일의 정보들을 가져옴
			MultipartFile file = (MultipartFile)entry.getValue();
			
			if(file.getSize() > 0) {
				
				String filename = System.currentTimeMillis()+"-"+cnt;
			
				//abc.jpg -> f2[0] = "abc"; f2[1] = "jpg";
				String f1 = file.getOriginalFilename();
				String[] f2 = f1.split("\\.");
				String exe = f2[f2.length-1];
				filename += "."+exe;
								  
				String filepath = path+ "/" + filename;
				
				//System.out.println("일흠 : " + filename);				//**
				//System.out.println("크기 : " + file.getSize());			//**
				//System.out.println("용량 : " + file.getBytes());
				//System.out.println("종류 : " + file.getContentType());	//**
				//System.out.println("이름 : " + file.getName());
				
				//실제 저장처리/파일 카피
				file.transferTo(new File(filepath));
				filenames += filename+"／";		// "／"  =>  "ㄱ" + 한자키 특수문자 사용
				
				//dto.setFilepath(dirname);
				//dto.setFilename(filename);
				//dto.setFilesize((int)file.getSize());
			}
			cnt++;
		}
		dto.setFilepath("/webapp/data");
		dto.setFilename(filenames);
		
		int result = dataService.insertDataBoard(dto);
		if(result == 0) message = "fail";
		
		return message;
	}
	
	
	
	@PostMapping("dataBoardUpdate")
	@ResponseBody
	public String updateDataBoard(MultipartHttpServletRequest request,DataDto dto) throws Exception {
		
		String message = "ok";
		
		//업로드 된 파일 이름(들)을 가져옴
		//   aa1.txt / bb1.txt / 
		String filename = dto.getFilename();
		
		//최종 변경 파일명이 들어갈 변수로 사용됨
		String[] str1 = new String[3];
		// str1[0]=null; str[1]=null; str[2]=null;
		
		// 기본 파일명(들)을 배열 처리함
		String[] str2 = filename.split("／");
		
		//str2 -> str1으로 값 넘김
		for(int i=0; i<str2.length; i++) {
			str1[i] = str2[i];
		}
		
		//str1[0] = "aa1.txt";
		//str1[1] = "bb1.txt";
		//str1[2] =  null;

		// 넘어온 파일(들)을 가져옴( 물리적 파일의 정보가 들어옴 )
		Map map = request.getFileMap();
		
		//Map의 키 값(들)을 가져옴
		Iterator it = map.entrySet().iterator();
		
		String filenames = "";
		int cnt = 0;
		
		while(it.hasNext()) {
			Entry entry = (Entry) it.next();
			
			//실제파일 가져옴
			MultipartFile file = (MultipartFile)entry.getValue();
			
			// abc.jpg -> array[2-1]
			String orgname = file.getOriginalFilename();
			
			if(orgname != null && !orgname.equals("")) {
				String[] array = orgname.split("\\.");
				String exe = array[array.length-1];		// 확장자
				
				String unix = System.currentTimeMillis()+"-"+cnt;
				String newname = unix+"."+exe;

				System.out.println("오리지널 :: " + orgname);
				System.out.println("새로운 :: " + newname);
				
				// input file 상자 이음 :: 예) myfile0 또는 myfile1 ..
				String boxname = file.getName();
				String boxnumber = boxname.replace("myfile","");
				int number = Integer.parseInt(boxnumber);
				
				String delname = str1[number];
				if(delname != null && !delname.equals("")) {
					File dfile = new File(path + "/" + delname);
					if(dfile.exists() == true) {
						dfile.delete();
					}
				}
				str1[number] = newname;
				
				file.transferTo(new File(path + "/" + newname));
				cnt++;
			}	// if close
			
		}	//while close
			
		//str1[0] = "aaaaaaa.txt";
		//str1[1] = null;
		//str1[2] = null;
		
		for(int i=0; i<str1.length; i++) {
			if(str1[i] != null && !str1[i].equals("")) {
				filenames += str1[i]+"／";
			}
		}
		
		System.out.println("filenames :::::::::::: "+filenames);
		
		dto.setFilename(filenames);
		
		int result = dataService.updateDataboard(dto);
		if(result == 0) message = "fail";
		
		return message;
	}
	
	
	
	@GetMapping("dataList")
	public String selectDataboardList(DefaultDto defaultDto, ModelMap model) throws Exception {
		
		//총 데이터 개수
		int total = dataService.selectDataboardTotal(defaultDto);
		//연산 처리를 위해 dto에 값을 세팅(보냄)
		defaultDto.setTotal(total);
		//총 페이지 개수를 계산시키는 장면
		defaultDto.setTotalPage();		
		
		// 데이터15개 :  (double)15/10 = 1.5 -> Math.ceil(1.5) -> (int)2.0 -> 2
		//계산 후 결과 얻어옴
		int totalPage = defaultDto.getTotalPage();
		
		// 출력 페이지 번호
		//int pageIndex = defaultDto.getPageIndex();
		
		// 출력 페이지 시작 행번호
		// 전체데이터수 - (페이지번호-1)*출력데이터수
		defaultDto.setPageRownum();
		int pageRownum = defaultDto.getPageRownum();
		
		//SQL에 적용할 시작번호
		//시작 번호의 계산 후 관련 변수에 값을 넣어 줌
		defaultDto.setFirstIndex();
		//SQL에 적용할 종료번호
		//끝 번호의 계산 후 관련 변수에 값을 넣어 줌		
		defaultDto.setLastIndex();
		
		//서비스 실행
		List<?> list = dataService.selectDataboardList(defaultDto);
		List<Map> list2 = new ArrayList<Map>();
		
		for(int i=0; i<list.size(); i++) {
			Map map = (Map)list.get(i);
			String files = (String)map.get("FILENAME");
			if(files != null) {
				files = filesNewMake(files);
				map.put("FILENAME",files);
			}
			list2.add(map);
		}
		
		model.addAttribute("resultList",list2);
		model.addAttribute("total",total);
		model.addAttribute("totalPage",totalPage);		//페이징
		model.addAttribute("pageRownum",pageRownum);	//페이지 가감처리
		
		return "board/dataList";
	}
	
	
	@GetMapping("dataDetail/{seqid}")
	public String selectDataboardDetail(@PathVariable int seqid,ModelMap model) throws Exception {
		
		//조회수 증가
		dataService.updateDataboardHits(seqid);
		
		DataDto dto = dataService.selectDataboardDetail(seqid);
		
		String files = dto.getFilename().trim();
		
		files = filesNewMake(files);
		
		dto.setFilename(files);
		
		model.addAttribute("dto",dto);
		
		return "board/dataDetail";
	}
	
	
	
	@GetMapping("dataModify/{seqid}")
	public String selectDataboardModify(@PathVariable int seqid, ModelMap model) throws Exception{
		
		DataDto dto = dataService.selectDataboardDetail(seqid);
		
		String files = dto.getFilename().trim();
		files = filesNewMake(files);
		dto.setFilename(files);
		model.addAttribute("dto",dto);
		
		return"board/dataModify";
	}
	
	
	
	@PostMapping("dataDelete")
	@ResponseBody
	public String deleteDataboard(DataDto dto) throws Exception {
		
		String message = "1";
		String filename = dto.getFilename();

		// 1.파일 삭제
		filename = filename.trim();
		if(filename != null && !filename.equals("")) {
			String[] files = filename.split("／");
			for(int i=0; i<files.length; i++) {
				String filepath = path+"/"+files[i];
				File file = new File(filepath);
				file.delete();
			}
		}
		
		// 2.삭제 서비스 실행
		int result = dataService.deleteDataboard(dto.getSeqid());
		if(result == 0) {
			message = "4";
		}
		
		return message;
	}
	
	
	
	@PostMapping("dataBoardFileDelete")
	@ResponseBody
	public String dataBoardFileDelete(DataDto dto,String delname) throws Exception {

		System.out.println("시퀀스 : " + dto.getSeqid());
		System.out.println("업뎃 파일명 : " + dto.getFilename());
		System.out.println("삭제 파일명 : " + delname);
		
		String message = "ok";
		
		String f1 = path+"/"+delname;
		File file = new File(f1);
		
		if(file.exists() == true) {
			//백업
			file.delete();
		}
		int result = dataService.updateDataboardFilename(dto);
		if(result == 0) message = "fail";
		
		return message;
	}
	
	
	
	public static String filesNewMake(String files) {
		
		if(!files.equals("")) {
			String[] str = files.split("／");
			for(int i = 0; i < str.length; i++) {
				String fname = str[i];
				String filepath = path + "/" + fname;
				
				//존재여부
				File file = new File(filepath);
				if(file.exists() == false) {
					files = files.replace(fname+"／", "");
				}
			}
		}
		return files;
	}
	
	
	

}


































