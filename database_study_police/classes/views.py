from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import JSONParser, MultiPartParser, FormParser
from rest_framework import status
from .serializers import CreateClassSerializer, ClassroomDetailsSerializer, ClassroomStudentPostSerializer, JoinedClassSerializer, ClassStudentDetailsSerializer
from .models import Classroom, ClassHasStudent


class CreateClassroom(APIView):

    def post(self, request, *args, **kwargs):

        '''
        Creates a row in the @model Classroom
        @request contains name, access_code, creator
        '''

        serializer = CreateClassSerializer(data = request.data)

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status = status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status = status.HTTP_400_BAD_REQUEST)



class ClassroomDetailsView(APIView):
    

    def get(self, request, creator):
        '''Returns info of classes created by a specific user, from the @model Classroom'''

        result = None
        
        if creator is not None:
            result = Classroom.objects.filter(creator = creator).order_by('name')
        
        if result is not None:
            serializer = ClassroomDetailsSerializer(result, many = True)

        return Response(serializer.data, status = status.HTTP_200_OK)


class GetAClassDataView(APIView):

    def get(self, request, id):
        '''Returns info of a specific class @model Classroom'''

        result = None

        if id is not None:
            result = Classroom.objects.get(id = id)

        if result is not None:
            serializer = ClassroomDetailsSerializer(result)

        return Response(serializer.data, status = status.HTTP_200_OK)


class AddStudentView(APIView):

    def post(self, request, student, access_code):
        '''
        Creates a row in the @model ClassHasStudent
        after getting the specific classroom from 'access_code'
        '''

        classroom = None
        serializer = None

        if access_code is not None:
            classroom = Classroom.objects.get(access_code = access_code)

        if classroom is not None and not ClassHasStudent.objects.filter(classroom = classroom.id, student = student).exists():
            serializer = ClassroomStudentPostSerializer(data = {"student":request.data['student'], "classroom": classroom.id})

        if serializer is None:
            return Response({"error":"bad request"}, status = status.HTTP_400_BAD_REQUEST)

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status = status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status = status.HTTP_400_BAD_REQUEST)


class JoinedClassesView(APIView):

    def get(self, request, student):
        '''
        Returns info of classes joined by a specific user, from the @model ClassHasStudent
        More details from the @model Classroom is retrieved using @serializer JoinedClassSerializer
        '''

        result = None

        if student is not None:
            result = ClassHasStudent.objects.filter(student = student)

        if result is not None:
            serializer = JoinedClassSerializer(result, many = True)

        return Response(serializer.data, status = status.HTTP_200_OK)


class ClassStudentsView(APIView):

    def get(self, request, classroom):
        '''
        Returns info of stutent joined in a specific class, from the @model ClassHasStudent
        More details from the @model Classroom is retrieved using @serializer ClassStudentDetailsSerializer
        '''

        result = None

        if classroom is not None:
            result = ClassHasStudent.objects.filter(classroom = classroom)

        if result is not None:
            serializer = ClassStudentDetailsSerializer(result, many = True)

        return Response(serializer.data, status = status.HTTP_200_OK)



