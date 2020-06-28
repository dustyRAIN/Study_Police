from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import JSONParser, MultiPartParser, FormParser, FileUploadParser
from rest_framework import status
from .models import ReadingTime, ContentFrequency
from .serializers import AddTimeSerializer, ModifyFrequencySerializer, FrequecySerializer
from users.models import CustomUser
from materials.models import Materials
from classes.models import ClassHasStudent, Classroom
from array import *
import numpy
import json

class StudentReadingTimeView(APIView):
    '''
    Defines get method to get all the reading time information of student(s) of a classroom
    '''

    def get(self, request):

        '''
        This method returns the reading time of a sepcific student or all students against a specific material or all
        materials.

        @param request should contain 'classroom', 'student', 'material'
        '''

        classroom = request.data['classroom']
        student = request.data['student']
        material = request.data['material']

        result = ReadingTime.objects.filter(classroom = classroom)

        if int(student) > 0 and int(material) > 0:
            result = ReadingTime.objects.filter(classroom = classroom, student = student, material = material)
        elif int(student) > 0:
            result = ReadingTime.objects.filter(classroom = classroom, student = student)
        elif int(material) > 0:
            result = ReadingTime.objects.filter(classroom = classroom, material = material)

        dur_of_student = {"-1" : 0}
        student_id = []

        if int(student) < 0:
            all_student = ClassHasStudent.objects.filter(classroom = classroom)
        else:
            all_student = ClassHasStudent.objects.filter(classroom = classroom, student = int(student))

        for stu in all_student:
            if str(stu.getStuId()) not in dur_of_student:
                dur_of_student[str(stu.getStuId())] = 0
                student_id.append(str(stu.getStuId()))
                


        for readtime in result:
            if str(readtime.student) in dur_of_student:
                dur_of_student[str(readtime.student)] += int(readtime.duration)
            else:
                dur_of_student[str(readtime.student)] = int(readtime.duration)
                student_id.append(str(readtime.student))
            

        student_id = numpy.unique(student_id)

        response = '[ '
        not_empty = False

        for stud in student_id:
            if not_empty:
                response += ', '
            
            not_empty = True
            stu = CustomUser.objects.get(email = stud)
            data = '{ '  + '"id" : ' + str(stu.id) + ' , ' + '"name" : "' + str(stu.name) + '" , ' +  '"duration" : ' + str(dur_of_student[stud]) + ' } '
            response += data

            

        response += ' ]'

        resObj = json.loads(response)
        #print(response)

        return Response(resObj, status = status.HTTP_200_OK)



class MaterialReadingTimeView(APIView):

    '''
    Defines get method to get all the reading time information of material(s) of a classroom
    '''

    def get(self, request):

        '''
        This method returns the reading time of a sepcific material or all materials against a specific student or all
        students.

        @param request should contain 'classroom', 'student', 'material'
        '''

        classroom = request.data['classroom']
        student = request.data['student']
        material = request.data['material']

        result = ReadingTime.objects.filter(classroom = classroom)

        if int(student) > 0 and int(material) > 0:
            result = ReadingTime.objects.filter(classroom = classroom, student = student, material = material)
        elif int(student) > 0:
            result = ReadingTime.objects.filter(classroom = classroom, student = student)
        elif int(material) > 0:
            result = ReadingTime.objects.filter(classroom = classroom, material = material)

        dur_of_material = {"-1" : 0}
        material_id = []

        if int(material) < 0:
            all_material = Materials.objects.filter(classroom = classroom)
        else:
            all_material = Materials.objects.filter(classroom = classroom, id = int(material))

        for mat in all_material:
            if str(mat.getMatId()) not in dur_of_material:
                dur_of_material[str(mat.getMatId())] = 0
                material_id.append(str(mat.getMatId()))

        for readtime in result:
            
            if str(readtime.material.getMatId()) in dur_of_material:
                dur_of_material[str(readtime.material.getMatId())] += int(readtime.duration)
            else:
                dur_of_material[str(readtime.material.getMatId())] = int(readtime.duration)
                material_id.append(str(readtime.material.getMatId()))
            

        material_id = numpy.unique(material_id)

        response = '[ '
        not_empty = False

        for mate in material_id:
            if not_empty:
                response += ', '
            
            not_empty = True
            mat = Materials.objects.get(id = int(mate))
            data = '{ '  + '"id" : ' + str(mat.id) + ' , ' + '"name" : "' + str(mat.name) + '" , ' +  '"duration" : ' + str(dur_of_material[mate]) + ' } '
            response += data

            

        response += ' ]'

        resObj = json.loads(response)
        #print(response)

        return Response(resObj, status = status.HTTP_200_OK)


class AddReadingTimeView(APIView):

    '''Defines post method to store specific student's reading time on a specific material in a specific classroom'''

    def post(self, request):

        '''
        This method add the new reading time to the previous reading time of a certain material for a certain student 
        in a certain classroom

        @param request should contain 'classroom', 'student', 'material', 'duration'
        '''

        serializer = AddTimeSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ContentFrequencyView(APIView):

    '''Defines post method to update the frequency of accessing a content'''

    def post(self, request):

        '''
        This method updates the frequency of a certain content such as material, classroom

        @param request should contain 'content_id', 'content_type', 'user', 'frequency'
        '''

        content_type = request.data['content_type']
        content_id = request.data['content_id']
        user = request.data['user']

        if content_id is not None and content_type is not None and user is not None:
            if not ContentFrequency.objects.filter(content_id = content_id, content_type = content_type, user = user).exists():
                
                if int(content_type) == 1:
                    if Materials.objects.filter(id = content_id).exists():
                        mat = Materials.objects.get(id = content_id)
                        _mutable = request.data._mutable
                        request.data._mutable = True
                        request.data['name'] = str(mat.name)
                        request.data._mutable = _mutable
                    else:
                        return Response({"details" : "material doesn't exist"}, status=status.HTTP_400_BAD_REQUEST)

                elif int(content_type) == 2 or int(content_type) == 3:
                    if Classroom.objects.filter(id = content_id).exists():
                        clas = Classroom.objects.get(id = content_id)
                        _mutable = request.data._mutable
                        request.data._mutable = True
                        request.data['name'] = str(clas.name)
                        request.data._mutable = _mutable
                    else:
                        return Response({"details" : "class doesn't exist"}, status=status.HTTP_400_BAD_REQUEST)


                serializer = ModifyFrequencySerializer(data=request.data)
                if serializer.is_valid():
                    serializer.save()
                    return Response(serializer.data, status=status.HTTP_200_OK)
                else:
                    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

            else:
                content = ContentFrequency.objects.get(content_id = content_id, content_type = content_type, user = user)
                new_freq = int(content.frequency) + int(request.data['frequency'])
                content.frequency = new_freq
                content.save()
                return Response({"new_frequncy" : new_freq}, status=status.HTTP_200_OK)


class GetUserFrequentContentView(APIView):

    '''Defines get method to return the frequently accessed contents'''

    def get(self, request):
        user = request.data['user']
        results = ContentFrequency.objects.filter(user = user).order_by('-frequency', 'content_type')

        serializer = FrequecySerializer(results, many=True)

        return Response(serializer.data, status = status.HTTP_200_OK)
